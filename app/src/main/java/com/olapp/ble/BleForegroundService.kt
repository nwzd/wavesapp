package com.olapp.ble

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
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
private const val NOTIF_ID_NEARBY = 1002   // single aggregated nearby notification
private const val NOTIF_ID_OLA_BASE   = 2000
private const val NOTIF_ID_MATCH_BASE = 3000
private const val EVICT_INTERVAL_MS          = 60 * 60 * 1000L
private const val RECEIVED_OLA_TTL_MS        = 30L * 24 * 60 * 60 * 1000L
private const val SENT_OLA_TTL_MS            = 30L * 24 * 60 * 60 * 1000L
private const val SERVICE_CHANNEL = "ola_ble"
private const val OLA_CHANNEL = "ola_received"
private const val MATCH_CHANNEL = "ola_match"
private const val NEARBY_CHANNEL = "ola_nearby"
private const val QUIET_HOUR_START = 22
private const val QUIET_HOUR_END   = 8
// Brand blue in ARGB for notification accent
private const val BRAND_COLOR = 0xFF0EA5E9.toInt()

@AndroidEntryPoint
class BleForegroundService : Service() {

    @Inject lateinit var nearbyManager: NearbyManager
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var appPreferences: com.olapp.data.preferences.AppPreferences

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var profileJob: Job? = null
    private var olaJob: Job? = null
    private var matchJob: Job? = null
    private var locationJob: Job? = null
    private var evictJob: Job? = null
    private var nearbyJob: Job? = null
    private var matchConfirmJob: Job? = null
    private var blockReceivedJob: Job? = null

    private val notifiedOlaTokens    = java.util.Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<String, Boolean>())
    private val notifiedMatchTokens  = java.util.Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<String, Boolean>())
    private val notifiedNearbyTokens = java.util.Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<String, Boolean>())
    private var nearbyNotifShownThisSession = false

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    Log.d(TAG, "Bluetooth turning off — releasing Nearby immediately")
                    nearbyManager.stop()
                    // Second call after 300 ms — gives the Nearby SDK time to fully free BT resources
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        nearbyManager.stop()
                    }, 300)
                }
                BluetoothAdapter.STATE_ON -> {
                    Log.d(TAG, "Bluetooth back on — restarting discovery")
                    scope.launch {
                        val profile = userRepository.getMyProfile() ?: return@launch
                        if (profile.discoveryEnabled) nearbyManager.startDiscoveryAndAdvertising(profile)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIF_ID, buildNotification())
        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

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
            var prevTokens = emptySet<String>()
            userRepository.observeMatches().collect { matches ->
                val current = matches.map { it.otherBleToken }.toHashSet()
                nearbyManager.matchedTokens = current
                // When a vibe is deleted, allow re-notification if they vibe again
                val removed = prevTokens - current
                if (removed.isNotEmpty()) {
                    notifiedMatchTokens.removeAll(removed)
                    notifiedOlaTokens.removeAll(removed)
                }
                prevTokens = current
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

        evictJob = scope.launch {
            while (true) {
                delay(EVICT_INTERVAL_MS)
                val now = System.currentTimeMillis()
                userRepository.evictStaleReceivedOlas(now - RECEIVED_OLA_TTL_MS)
                userRepository.evictStaleSentOlas(now - SENT_OLA_TTL_MS)
            }
        }

        nearbyJob = scope.launch {
            nearbyManager.peers.collect { peers ->
                val newPeers = peers.values.filter { notifiedNearbyTokens.add(it.bleToken) }
                if (newPeers.isNotEmpty() && !isQuietHours()) {
                    showNearbyNotification(notifiedNearbyTokens.size, newPeers.first().displayName)
                }
            }
        }

        // Load blocked tokens and block score so NearbyManager can enforce them
        scope.launch {
            nearbyManager.blockedTokens = userRepository.getBlockedTokens()
        }
        scope.launch {
            nearbyManager.blockScore = appPreferences.getBlockScore()
        }

        blockReceivedJob = scope.launch {
            nearbyManager.blockReceivedFlow.collect { fromToken ->
                val count = appPreferences.addBlockReceivedFrom(fromToken)
                nearbyManager.blockScore = count
                Log.d(TAG, "Block score updated: $count (from $fromToken)")
            }
        }

        matchConfirmJob = scope.launch {
            nearbyManager.matchConfirmReceived.collect { data ->
                if (!userRepository.hasMatchWith(data.token)) {
                    val location = captureLocation()
                    userRepository.createMatch(
                        otherBleToken = data.token,
                        otherDisplayName = data.displayName,
                        otherContactInfo = data.contactInfo,
                        otherPhotoUrl = data.photoPath ?: "",
                        latitude = location?.first,
                        longitude = location?.second
                    )
                    Log.d(TAG, "Match created via confirmation from ${data.displayName}")
                }
                // Notify regardless of who created the match — covers the case where the
                // local side already created it (wave-back initiator) but still needs a ping.
                if (notifiedMatchTokens.add(data.token)) showMatchNotification(data.token, data.displayName)
            }
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP        -> { stopSelf(); return START_NOT_STICKY }
            ACTION_CLEAR_STATE -> {
                notifiedOlaTokens.clear()
                notifiedMatchTokens.clear()
                notifiedNearbyTokens.clear()
                nearbyNotifShownThisSession = false
                getSystemService(NotificationManager::class.java).cancel(NOTIF_ID_NEARBY)
                Log.d(TAG, "Notification state cleared")
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(bluetoothReceiver) }
        profileJob?.cancel()
        olaJob?.cancel()
        matchJob?.cancel()
        locationJob?.cancel()
        evictJob?.cancel()
        nearbyJob?.cancel()
        matchConfirmJob?.cancel()
        blockReceivedJob?.cancel()
        serviceJob.cancel()
        nearbyManager.stop()
        Log.d(TAG, "Service destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Restart the service 1 s after the app is swiped away
        val restart = Intent(applicationContext, BleForegroundService::class.java)
        val pi = PendingIntent.getService(
            this, 1, restart, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1_000L, pi)
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ------------------------------------------------------------------
    // OLA handling
    // ------------------------------------------------------------------

    private suspend fun handleOlaReceived(senderToken: String, senderName: String) {
        val profile = userRepository.getMyProfile() ?: return
        if (senderToken == profile.bleToken) return

        // Already matched — send a match confirmation so they can recreate their side
        // (do NOT send OLA back; that would create an infinite OLA ping-pong loop).
        if (userRepository.hasMatchWith(senderToken)) {
            nearbyManager.endpointIdForToken(senderToken)
                ?.let { nearbyManager.sendMatchConfirmation(it) }
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
            nearbyManager.clearPendingOla(senderToken)
            nearbyManager.endpointIdForToken(senderToken)?.let { eid ->
                nearbyManager.sendMatchConfirmation(eid)
                if (location != null) nearbyManager.sendMatchLocation(eid, location.first, location.second)
            }
            Log.d(TAG, "Auto-match with \"$senderName\"")
            if (isNew && notifiedMatchTokens.add(senderToken)) showMatchNotification(senderToken, senderName)
        } else if (isNew && notifiedOlaTokens.add(senderToken)) {
            showOlaNotification(senderToken, senderName)
        }
    }

    private fun showMatchNotification(senderToken: String, senderName: String) {
        if (!hasNotifPermission()) return
        val notifId = NOTIF_ID_MATCH_BASE + (senderToken.hashCode().and(0x7FFFFFFF) % 1000)
        val name = senderName.ifBlank { "Someone" }
        val notif = NotificationCompat.Builder(this, MATCH_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle("It's a vibe with $name")
            .setContentText("You both waved — open Wave & Vibe to connect")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainActivityIntent(notifId))
            .build()
        getSystemService(NotificationManager::class.java).notify(notifId, notif)
    }

    private fun showOlaNotification(senderToken: String, senderName: String) {
        if (!hasNotifPermission()) return
        val notifId = NOTIF_ID_OLA_BASE + (senderToken.hashCode().and(0x7FFFFFFF) % 1000)
        val name = senderName.ifBlank { "Someone" }
        val notif = NotificationCompat.Builder(this, OLA_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle("$name sent you a wave")
            .setContentText("Open Wave & Vibe to wave back")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainActivityIntent(notifId))
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
    // Nearby notification
    // ------------------------------------------------------------------

    private fun isQuietHours(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour >= QUIET_HOUR_START || hour < QUIET_HOUR_END
    }

    // Single aggregated nearby notification — always reuses NOTIF_ID_NEARBY.
    // totalSeen = total unique tokens seen this session; firstName = first new arrival's name.
    private fun showNearbyNotification(totalSeen: Int, firstName: String) {
        if (!hasNotifPermission()) return
        val name = firstName.ifBlank { "Someone" }
        val title = if (totalSeen == 1) "$name is nearby" else "$totalSeen people nearby"
        val text  = "Open Wave & Vibe to say hello"
        val notif = NotificationCompat.Builder(this, NEARBY_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(BRAND_COLOR)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setOnlyAlertOnce(nearbyNotifShownThisSession)  // vibrate/sound only on first
            .setContentIntent(mainActivityIntent(NOTIF_ID_NEARBY))
            .build()
        nearbyNotifShownThisSession = true
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID_NEARBY, notif)
    }

    private fun hasNotifPermission() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun mainActivityIntent(requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            this, requestCode,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    // ------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------

    private fun buildNotification() = NotificationCompat.Builder(this, SERVICE_CHANNEL)
        .setContentTitle(getString(R.string.app_name))
        .setContentText(getString(R.string.ble_notification_text))
        .setSmallIcon(R.drawable.ic_notification)
        .setColor(BRAND_COLOR)
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
            nm.createNotificationChannel(
                NotificationChannel(NEARBY_CHANNEL, "Someone nearby",
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "A new person is nearby"
                    }
            )
        }
    }

    companion object {
        const val ACTION_STOP        = "com.olapp.BLE_STOP"
        const val ACTION_CLEAR_STATE = "com.olapp.BLE_CLEAR_STATE"

        fun start(context: Context) {
            val i = Intent(context, BleForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(i)
            else context.startService(i)
        }

        fun stop(context: Context) =
            context.startService(Intent(context, BleForegroundService::class.java).apply { action = ACTION_STOP })

        fun clearState(context: Context) =
            context.startService(Intent(context, BleForegroundService::class.java).apply { action = ACTION_CLEAR_STATE })
    }
}
