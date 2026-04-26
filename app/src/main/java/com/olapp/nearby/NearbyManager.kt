package com.olapp.nearby

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.olapp.data.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "NearbyManager"
private const val SERVICE_ID = "com.olapp.nearby"

// Message types
private const val MSG_MINI          = "m"   // name/token/desc/contact — tiny, sent first
private const val MSG_THUMB         = "t"   // low-res thumbnail — sent immediately after identity
private const val MSG_PHOTO         = "h"   // standard photo — sent ~300 ms after thumbnail
private const val MSG_PHOTO_HD      = "H"   // HD photo — sent only on explicit request
private const val MSG_PHOTO_REQ     = "r"   // request peer to send their HD photo
private const val MSG_PROFILE       = "P"   // legacy full profile (handle if received)
private const val MSG_OLA           = "O"
private const val MSG_MATCH_LOC     = "L"   // match location share — sent after match is created
private const val MSG_MATCH_CONFIRM = "MC"  // sent by whoever detects mutual match; other side creates their match

// Thumbnail: very small, arrives in <100 ms — keeps discovery instant
private const val THUMB_MAX_PX       = 40
private const val THUMB_JPEG_QUALITY = 20
// Standard photo: decent quality, sent after a short delay
private const val PHOTO_MAX_PX       = 128
private const val PHOTO_JPEG_QUALITY = 70
private const val PHOTO_DELAY_MS     = 100L
// Refresh BLE scan periodically to catch newly arrived peers
private const val SCAN_REFRESH_MS    = 12_000L
// HD photo: only sent on demand when the other user zooms in
private const val PHOTO_HD_MAX_PX       = 400
private const val PHOTO_HD_JPEG_QUALITY = 85

data class PendingPeer(val bleToken: String, val displayName: String)

data class MatchConfirmData(
    val token: String,
    val displayName: String,
    val contactInfo: String,
    val photoPath: String?
)

data class NearbyPeer(
    val endpointId: String,
    val bleToken: String,
    val displayName: String,
    val description: String = "",
    val contactInfo: String = "",
    val photoPath: String? = null
)

@Singleton
class NearbyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = Nearby.getConnectionsClient(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _peers = MutableStateFlow<Map<String, NearbyPeer>>(emptyMap())
    val peers: StateFlow<Map<String, NearbyPeer>> = _peers.asStateFlow()

    private val _pendingEndpoints = MutableStateFlow<Map<String, PendingPeer>>(emptyMap())
    val pendingEndpoints: StateFlow<Map<String, PendingPeer>> = _pendingEndpoints.asStateFlow()

    val olaReceived = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 8)
    val locationReceived = MutableSharedFlow<Triple<String, Double, Double>>(extraBufferCapacity = 8)
    val matchConfirmReceived = MutableSharedFlow<MatchConfirmData>(extraBufferCapacity = 8)

    @Volatile var myToken: String = ""
    @Volatile var myDisplayName: String = ""
    @Volatile var matchedTokens: Set<String> = emptySet()
    @Volatile private var currentProfile: UserProfile? = null
    private var scanRefreshJob: Job? = null

    // Cached outbound payloads — rebuilt whenever profile changes
    @Volatile private var cachedMiniJson: String = """{"t":"m","tok":"","n":"","d":"","c":""}"""
    @Volatile private var cachedThumbJson: String? = null   // 40px/q20, sent immediately
    @Volatile private var cachedPhotoJson: String? = null   // 128px/q70, sent after 300ms
    @Volatile private var cachedHdJson: String? = null      // 400px/q85, sent only on request

    fun updateProfile(profile: UserProfile) {
        myToken = profile.bleToken
        myDisplayName = profile.displayName
        cachedMiniJson = buildMiniJson(profile)
        cachedThumbJson = buildPhotoJson(profile, THUMB_MAX_PX, THUMB_JPEG_QUALITY, MSG_THUMB)
        cachedPhotoJson = buildPhotoJson(profile, PHOTO_MAX_PX, PHOTO_JPEG_QUALITY, MSG_PHOTO)
        cachedHdJson = buildPhotoJson(profile, PHOTO_HD_MAX_PX, PHOTO_HD_JPEG_QUALITY, MSG_PHOTO_HD)
    }

    fun startDiscoveryAndAdvertising(profile: UserProfile) {
        updateProfile(profile)
        currentProfile = profile
        runCatching { client.stopAdvertising() }
        runCatching { client.stopDiscovery() }
        doStartAdvertising(profile)
        doStartDiscovery()
        startScanRefresh()
    }

    fun stop() {
        scanRefreshJob?.cancel()
        scanRefreshJob = null
        _peers.value.keys.forEach { runCatching { client.disconnectFromEndpoint(it) } }
        runCatching { client.stopAllEndpoints() }
        runCatching { client.stopAdvertising() }
        runCatching { client.stopDiscovery() }
        _peers.value = emptyMap()
        _pendingEndpoints.value = emptyMap()
    }

    private fun startScanRefresh() {
        scanRefreshJob?.cancel()
        scanRefreshJob = scope.launch {
            while (true) {
                delay(SCAN_REFRESH_MS)
                // Only restart discovery — stopping advertising would fire onEndpointLost
                // on peers, causing visible flicker in their Nearby list.
                runCatching { client.stopDiscovery() }
                delay(150)
                doStartDiscovery()
            }
        }
    }

    fun sendOla(endpointId: String) {
        val msg = JSONObject().apply {
            put("t", MSG_OLA)
            put("tok", myToken)
            put("n", myDisplayName)
        }
        send(endpointId, msg.toString())
    }

    fun requestHdPhoto(endpointId: String) {
        val msg = JSONObject().apply { put("t", MSG_PHOTO_REQ); put("tok", myToken) }
        send(endpointId, msg.toString())
    }

    fun sendMatchLocation(endpointId: String, lat: Double, lon: Double) {
        val msg = JSONObject().apply {
            put("t", MSG_MATCH_LOC)
            put("tok", myToken)
            put("lat", lat)
            put("lon", lon)
        }
        send(endpointId, msg.toString())
    }

    fun sendMatchConfirmation(endpointId: String) {
        val profile = currentProfile ?: return
        val msg = JSONObject().apply {
            put("t", MSG_MATCH_CONFIRM)
            put("tok", myToken)
            put("n", myDisplayName)
            put("c", profile.contactInfo)
        }
        send(endpointId, msg.toString())
    }

    fun endpointIdForToken(token: String): String? =
        _peers.value.values.find { it.bleToken == token }?.endpointId

    fun isTokenInRange(token: String): Boolean =
        _peers.value.values.any { it.bleToken == token }

    private val retryCount = mutableMapOf<String, Int>()

    private fun retryConnection(endpointId: String) {
        val attempts = retryCount.getOrDefault(endpointId, 0)
        if (attempts >= 3) {
            retryCount.remove(endpointId)
            return
        }
        retryCount[endpointId] = attempts + 1
        scope.launch {
            delay(10_000L)  // was 3 s — less aggressive radio wake-ups
            client.requestConnection(myToken, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener { retryCount.remove(endpointId) }
                .addOnFailureListener { Log.w(TAG, "retry $attempts failed $endpointId: $it") }
        }
    }

    // ------------------------------------------------------------------
    // Advertising / discovery
    // ------------------------------------------------------------------

    private fun doStartAdvertising(profile: UserProfile) {
        val name = "${profile.bleToken}|${profile.displayName.take(30)}"
        val opts = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        client.startAdvertising(name, SERVICE_ID, connectionLifecycleCallback, opts)
            .addOnSuccessListener { Log.d(TAG, "Advertising started") }
            .addOnFailureListener { Log.e(TAG, "Advertising failed: $it") }
    }

    private fun doStartDiscovery() {
        val opts = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, opts)
            .addOnSuccessListener { Log.d(TAG, "Discovery started") }
            .addOnFailureListener { Log.e(TAG, "Discovery failed: $it") }
    }

    // ------------------------------------------------------------------
    // Callbacks
    // ------------------------------------------------------------------

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Found $endpointId (${info.endpointName})")
            val parsed = parseCompactName(info.endpointName)
            if (parsed != null) {
                val (tok, name) = parsed
                if (tok in matchedTokens) {
                    Log.d(TAG, "Skipping $endpointId — already vibed with $tok")
                    return
                }
                _pendingEndpoints.update { it + (endpointId to PendingPeer(tok, name)) }
            }
            client.requestConnection(myToken, endpointId, connectionLifecycleCallback)
                .addOnFailureListener { Log.w(TAG, "requestConnection $endpointId: $it") }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Endpoint lost: $endpointId")
            _peers.update { it - endpointId }
            _pendingEndpoints.update { it - endpointId }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "Connection initiated $endpointId (${info.endpointName})")
            if (endpointId !in _pendingEndpoints.value) {
                parseCompactName(info.endpointName)?.let { (tok, name) ->
                    _pendingEndpoints.update { it + (endpointId to PendingPeer(tok, name)) }
                }
            }
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                retryCount.remove(endpointId)
                Log.d(TAG, "Connected $endpointId — sending identity, thumb, then full photo")
                // 1. Identity — peer appears in list immediately
                send(endpointId, cachedMiniJson)
                // 2. Low-res thumbnail — avatar visible in <100 ms
                cachedThumbJson?.let { send(endpointId, it) }
                // 3. Full-res photo — sent after a short delay to avoid congestion
                val fullPhoto = cachedPhotoJson ?: return
                scope.launch {
                    delay(PHOTO_DELAY_MS)
                    send(endpointId, fullPhoto)
                }
            } else {
                Log.w(TAG, "Connection failed $endpointId: ${result.status.statusCode}")
                _pendingEndpoints.update { it - endpointId }
                retryConnection(endpointId)
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected $endpointId")
            _peers.update { it - endpointId }
            _pendingEndpoints.update { it - endpointId }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type != Payload.Type.BYTES) return
            val bytes = payload.asBytes() ?: return
            runCatching {
                val json = JSONObject(String(bytes))
                when (json.optString("t")) {
                    MSG_MINI, MSG_PROFILE -> handleIdentity(endpointId, json)
                    MSG_THUMB, MSG_PHOTO  -> handlePhoto(endpointId, json, hd = false)
                    MSG_PHOTO_HD          -> handlePhoto(endpointId, json, hd = true)
                    MSG_PHOTO_REQ         -> cachedHdJson?.let { send(endpointId, it) }
                    MSG_OLA               -> handleOla(json)
                    MSG_MATCH_LOC         -> handleMatchLocation(json)
                    MSG_MATCH_CONFIRM     -> handleMatchConfirm(endpointId, json)
                    else                  -> Unit
                }
            }.onFailure { Log.e(TAG, "Payload parse error from $endpointId", it) }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    // ------------------------------------------------------------------
    // Payload handlers
    // ------------------------------------------------------------------

    private fun handleIdentity(endpointId: String, json: JSONObject) {
        val token   = json.optString("tok").ifEmpty { return }
        val name    = json.optString("n")
        val desc    = json.optString("d")
        val contact = json.optString("c")
        // Legacy full-profile may include photo inline
        val photoPath = json.optString("photo").takeIf { it.isNotEmpty() }
            ?.let { savePhoto(token, it) }

        _peers.update { map ->
            // Preserve existing photo if we already have one (from a previous MSG_PHOTO)
            val existing = map[endpointId]
            map + (endpointId to NearbyPeer(
                endpointId, token, name, desc, contact,
                photoPath ?: existing?.photoPath
            ))
        }
        _pendingEndpoints.update { it - endpointId }
        Log.d(TAG, "Identity from $endpointId: $name ($token)")
    }

    private fun handlePhoto(endpointId: String, json: JSONObject, hd: Boolean) {
        val token = json.optString("tok").ifEmpty { return }
        val suffix = if (hd) "_hd" else ""
        val photoPath = json.optString("photo").takeIf { it.isNotEmpty() }
            ?.let { savePhoto(token, it, suffix) } ?: return
        _peers.update { map ->
            val peer = map[endpointId] ?: return@update map
            map + (endpointId to peer.copy(photoPath = photoPath))
        }
        Log.d(TAG, "${if (hd) "HD photo" else "Photo"} received from $token")
    }

    private fun handleOla(json: JSONObject) {
        val tok  = json.optString("tok").ifEmpty { return }
        // Prefer the name embedded in the message; fall back to peer lookup for older clients
        val name = json.optString("n").ifEmpty {
            _peers.value.values.find { it.bleToken == tok }?.displayName ?: ""
        }
        Log.d(TAG, "Wave from $tok ($name)")
        olaReceived.tryEmit(tok to name)
    }

    private fun handleMatchConfirm(endpointId: String, json: JSONObject) {
        val tok     = json.optString("tok").ifEmpty { return }
        val name    = json.optString("n")
        val contact = json.optString("c")
        val photo   = _peers.value[endpointId]?.photoPath
        Log.d(TAG, "Match confirmation from $tok ($name)")
        matchConfirmReceived.tryEmit(MatchConfirmData(tok, name, contact, photo))
    }

    private fun handleMatchLocation(json: JSONObject) {
        val tok = json.optString("tok").ifEmpty { return }
        val lat = json.optDouble("lat", Double.NaN).takeIf { !it.isNaN() } ?: return
        val lon = json.optDouble("lon", Double.NaN).takeIf { !it.isNaN() } ?: return
        Log.d(TAG, "Match location from $tok: $lat, $lon")
        locationReceived.tryEmit(Triple(tok, lat, lon))
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun send(endpointId: String, json: String) {
        runCatching {
            client.sendPayload(endpointId, Payload.fromBytes(json.toByteArray()))
        }.onFailure { Log.e(TAG, "send failed to $endpointId", it) }
    }

    private fun parseCompactName(name: String): Pair<String, String>? {
        val idx = name.indexOf('|')
        if (idx < 1) return null
        return name.substring(0, idx) to name.substring(idx + 1)
    }

    private fun buildMiniJson(profile: UserProfile): String = JSONObject().apply {
        put("t", MSG_MINI)
        put("tok", profile.bleToken)
        put("n", profile.displayName)
        put("d", profile.description)
        put("c", profile.contactInfo)
    }.toString()

    private fun buildPhotoJson(profile: UserProfile, maxPx: Int, quality: Int, msgType: String): String? {
        val file = profile.photoUrl.takeIf { it.isNotBlank() }?.let { File(it) }
        if (file?.exists() != true) return null
        val b64 = compressPhotoToBase64(file, maxPx, quality) ?: return null
        return JSONObject().apply {
            put("t", msgType)
            put("tok", profile.bleToken)
            put("photo", b64)
        }.toString()
    }

    private fun compressPhotoToBase64(file: File, maxPx: Int, quality: Int): String? = runCatching {
        val original = BitmapFactory.decodeFile(file.absolutePath) ?: return@runCatching null
        val scale = maxPx.toFloat() / maxOf(original.width, original.height).coerceAtLeast(1)
        val w = (original.width * scale).toInt().coerceAtLeast(1)
        val h = (original.height * scale).toInt().coerceAtLeast(1)
        val resized = Bitmap.createScaledBitmap(original, w, h, true)
        val out = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, quality, out)
        if (resized !== original) resized.recycle()
        original.recycle()
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }.getOrNull()

    private fun savePhoto(token: String, base64: String, suffix: String = ""): String? = runCatching {
        val bytes = Base64.decode(base64, Base64.NO_WRAP)
        val dir = File(context.filesDir, "peer_photos").also { it.mkdirs() }
        val file = File(dir, "$token$suffix.jpg")
        FileOutputStream(file).use { it.write(bytes) }
        file.absolutePath
    }.getOrElse { e -> Log.e(TAG, "savePhoto failed for $token", e); null }
}
