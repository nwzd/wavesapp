package com.olapp.ble

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.withTimeoutOrNull
import com.olapp.MainActivity
import com.olapp.R
import com.olapp.data.repository.UserRepository
import com.olapp.nearby.NearbyManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

private const val TAG = "BleForegroundService"
private const val NOTIF_ID = 1001
private const val TOKEN_REFRESH_INTERVAL_MS = 15 * 60 * 1000L
private const val EVICT_INTERVAL_MS = 120_000L   // 2 min — was 30 s
private const val NEARBY_TTL_MS = 60_000L
private const val SERVICE_CHANNEL = "ola_ble"
private const val OLA_CHANNEL = "ola_received"
private const val MATCH_CHANNEL = "ola_match"

@AndroidEntryPoint
class BleForegroundService : Service() {

    @Inject lateinit var nearbyManager: NearbyManager
    @Inject lateinit var userRepository: UserRepository

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var profileJob: Job? = null
    private var olaJob: Job? = null
    private var matchJob: Job? = null
    private var locationJob: Job? = null
    private var tokenRefreshJob: Job? = null
    private var evictJob: Job? = null

    // Track tokens we've already notified this session to avoid repeat notifications
    private val notifiedOlaTokens = HashSet<String>()
    private val notifiedMatchTokens = HashSet<String>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIF_ID, buildNotification())

        olaJob = scope.launch {
            nearbyManager.olaReceived.collect { (senderToken, senderName) ->
                handleOlaReceived(senderToken, senderName)
            }
        }

        locationJob = scope.launch {
            nearbyManager.locationReceived.collect { (bleToken, lat, lon) ->
                userRepository.updateMatchLocation(bleToken, lat, lon)
                Log.d(TAG, "Match location updated for $bleToken: $lat, $lon")
            }
        }

        matchJob = scope.launch {
            userRepository.observeMatches().collect { matches ->
                nearbyManager.matchedTokens = matches.map { it.otherBleToken }.toHashSet()
            }
        }

        // Only restart Nearby when discoveryEnabled toggles.
        // Token refresh and other profile changes just update cached data — no reconnection.
        profileJob = scope.launch {
            var prevEnabled: Boolean? = null
            userRepository.observeMyProfile().collect { profile ->
                if (profile == null) return@collect
                if (prevEnabled != profile.discoveryEnabled) {
                    if (profile.discoveryEnabled) nearbyManager.startDiscoveryAndAdvertising(profile)
                    else nearbyManager.stop()
                    prevEnabled = profile.discoveryEnabled
                } else {
                    nearbyManager.updateProfile(profile)
                }
                Log.d(TAG, "Profile: token=${profile.bleToken} visible=${profile.discoveryEnabled}")
            }
        }

        tokenRefreshJob = scope.launch {
            delay(TOKEN_REFRESH_INTERVAL_MS)
            while (true) {
                try {
                    val profile = userRepository.getMyProfile() ?: break
                    if (profile.discoveryEnabled) {
                        userRepository.refreshBleToken()
                        Log.d(TAG, "BLE token refreshed")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Token refresh failed", e)
                }
                delay(TOKEN_REFRESH_INTERVAL_MS)
            }
        }

        evictJob = scope.launch {
            while (true) {
                delay(EVICT_INTERVAL_MS)
                val cutoff = System.currentTimeMillis() - NEARBY_TTL_MS
                userRepository.evictStaleReceivedOlas(cutoff)
                userRepository.evictStaleSentOlas(cutoff)
            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        profileJob?.cancel()
        olaJob?.cancel()
        matchJob?.cancel()
        locationJob?.cancel()
        tokenRefreshJob?.cancel()
        evictJob?.cancel()
        serviceJob.cancel()
        nearbyManager.stop()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ------------------------------------------------------------------
    // OLA handling
    // ------------------------------------------------------------------

    private suspend fun handleOlaReceived(senderToken: String, senderName: String) {
        val profile = userRepository.getMyProfile() ?: return
        if (senderToken == profile.bleToken) return

        // If we still have a match with this person and they're re-sending, auto-respond so
        // they can re-create their side of the match without affecting ours.
        if (userRepository.hasMatchWith(senderToken)) {
            nearbyManager.endpointIdForToken(senderToken)
                ?.let { nearbyManager.sendOla(it) }
            return
        }

        val peer = nearbyManager.peers.value.values.find { it.bleToken == senderToken }
        val senderPhotoPath = peer?.photoPath?.takeIf { it.isNotEmpty() } ?: ""
        val senderContact = peer?.contactInfo ?: ""

        val saved = userRepository.saveReceivedOla(
            senderBleToken = senderToken,
            senderDisplayName = senderName,
            senderPhotoUrl = senderPhotoPath,
            senderContactInfo = senderContact,
            latitude = null,
            longitude = null
        )
        val isNew = System.currentTimeMillis() - saved.timestamp < 2_000L
        Log.d(TAG, "Wave from \"$senderName\" ($senderToken)")

        if (userRepository.hasSentOlaTo(senderToken)) {
            val existing = userRepository.getLatestReceivedOlaFrom(senderToken)
            val location = captureLocation()
            val resolvedName = senderName.ifEmpty { existing?.senderDisplayName ?: "" }
            val resolvedPhoto = senderPhotoPath.ifEmpty {
                existing?.senderPhotoUrl?.takeIf { it.isNotEmpty() } ?: ""
            }
            val resolvedContact = senderContact.ifEmpty { existing?.senderContactInfo ?: "" }
            userRepository.createMatch(
                otherBleToken = senderToken,
                otherDisplayName = resolvedName,
                otherPhotoUrl = resolvedPhoto,
                otherContactInfo = resolvedContact,
                latitude = location?.first,
                longitude = location?.second
            )
            if (location != null) {
                nearbyManager.endpointIdForToken(senderToken)
                    ?.let { nearbyManager.sendMatchLocation(it, location.first, location.second) }
            }
            Log.d(TAG, "Auto-match with \"$senderName\"")
            if (isNew && notifiedMatchTokens.add(senderToken)) showMatchNotification(senderToken, senderName)
        } else if (isNew && notifiedOlaTokens.add(senderToken)) {
            showOlaNotification(senderToken, senderName)
        }
    }

    private fun showMatchNotification(senderToken: String, senderName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val notifId = 3000 + (senderToken.hashCode().and(0x7FFFFFFF) % 1000)
        val displayName = senderName.ifBlank { "Someone" }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            this, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(this, MATCH_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setContentTitle("✨ It's a vibe!")
            .setContentText("You and $displayName both waved at each other")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        getSystemService(NotificationManager::class.java).notify(notifId, notif)
    }

    private fun showOlaNotification(senderToken: String, senderName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val notifId = 2000 + (senderToken.hashCode().and(0x7FFFFFFF) % 1000)
        val displayName = senderName.ifBlank { "Someone" }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            this, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, OLA_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setContentTitle("🌊 $displayName sent you a wave")
            .setContentText("Open Waves to wave back")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        getSystemService(NotificationManager::class.java).notify(notifId, notif)
    }

    // ------------------------------------------------------------------
    // Location — one-shot only at match time, no background polling
    // ------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    private suspend fun captureLocation(): Pair<Double, Double>? {
        val client = LocationServices.getFusedLocationProviderClient(this)

        // System cache — instant, populated by any app that recently used location
        val cached = try {
            suspendCancellableCoroutine<android.location.Location?> { cont ->
                client.lastLocation
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
            }
        } catch (e: SecurityException) { null }
        if (cached != null) return cached.latitude to cached.longitude

        // Active fix fallback — only if location services are on; 4 s timeout
        val lm = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        if (!lm.isLocationEnabled) return null

        return withTimeoutOrNull(4_000L) {
            val cts = CancellationTokenSource()
            suspendCancellableCoroutine<Pair<Double, Double>?> { cont ->
                cont.invokeOnCancellation { cts.cancel() }
                try {
                    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                        .addOnSuccessListener { loc -> cont.resume(if (loc != null) loc.latitude to loc.longitude else null) }
                        .addOnFailureListener { cont.resume(null) }
                } catch (e: SecurityException) { cont.resume(null) }
            }
        }
    }

    // ------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------

    private fun buildNotification() = NotificationCompat.Builder(this, SERVICE_CHANNEL)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(getString(R.string.ble_notification_text))
        .setSmallIcon(android.R.drawable.ic_menu_compass)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        )
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel, "Stop",
            PendingIntent.getService(this, 0,
                Intent(this, BleForegroundService::class.java).apply { action = ACTION_STOP },
                PendingIntent.FLAG_IMMUTABLE)
        )
        .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(SERVICE_CHANNEL, getString(R.string.ble_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW).apply { description = "Background discovery" }
            )
            nm.createNotificationChannel(
                NotificationChannel(OLA_CHANNEL, "Received waves",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "Someone sent you a wave"
                        enableVibration(true)
                    }
            )
            nm.createNotificationChannel(
                NotificationChannel(MATCH_CHANNEL, "New matches",
                    NotificationManager.IMPORTANCE_HIGH).apply {
                        description = "You and someone waved at each other"
                        enableVibration(true)
                    }
            )
        }
    }

    companion object {
        const val ACTION_STOP = "com.olapp.BLE_STOP"

        fun start(context: Context) {
            val i = Intent(context, BleForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
            else context.startService(i)
        }

        fun stop(context: Context) =
            context.startService(Intent(context, BleForegroundService::class.java).apply { action = ACTION_STOP })
    }
}
