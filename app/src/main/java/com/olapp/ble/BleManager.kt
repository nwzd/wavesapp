package com.olapp.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BLE payload layout (bytes after the 2-byte company ID):
 *
 *  PRESENCE (type = 0x00):
 *    [0]     MAGIC = 0x4F ('O')
 *    [1-8]   sender token  (8 bytes, 16 hex chars)
 *    [9]     type = 0x00
 *    [10]    name byte length
 *    [11+]   display-name UTF-8 (≤ 13 bytes)
 *    Total max: 24 bytes
 *
 *  OLA (type = 0x01):
 *    [0]     MAGIC = 0x4F ('O')
 *    [1-8]   sender token  (8 bytes)
 *    [9]     type = 0x01
 *    [10-17] target token  (8 bytes)
 *    Total: 18 bytes
 */
private const val TAG = "BleManager"
private const val MANUFACTURER_ID = 0x4F4C      // "OL"
private const val MAGIC: Byte = 0x4F            // 'O'
private const val TYPE_PRESENCE: Byte = 0x00
private const val TYPE_OLA: Byte = 0x01
private const val NAME_MAX_BYTES = 13
const val OLA_ADVERTISE_MS = 60_000L
private const val DEVICE_TTL_MS = 60_000L
private const val ADVERTISE_START_DELAY_MS = 200L

@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val adapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val bleHandler = Handler(Looper.getMainLooper())

    private val _nearbyTokens = MutableStateFlow<Map<String, String>>(emptyMap())
    val nearbyTokens: StateFlow<Map<String, String>> = _nearbyTokens.asStateFlow()

    private val lastSeen = ConcurrentHashMap<String, Long>()

    var myToken: String = ""
    var onOlaReceived: ((senderToken: String, senderDisplayName: String) -> Unit)? = null

    private var olaActiveUntil: Long = 0L
    fun isOlaActive(): Boolean = System.currentTimeMillis() < olaActiveUntil

    fun isTokenInRange(token: String): Boolean = _nearbyTokens.value.containsKey(token)

    // Current active advertise callback — stopped before each new advertisement
    private var activeAdvertiseCallback: AdvertiseCallback? = null
    // Pending payload to advertise after the stop-then-start delay
    @Volatile private var pendingPayload: ByteArray? = null

    // ------------------------------------------------------------------
    // Advertising
    // ------------------------------------------------------------------

    fun startPresenceAdvertising(token: String, displayName: String) {
        if (isOlaActive()) return
        scheduleAdvertise(buildPresencePayload(token, displayName))
        Log.d(TAG, "PRESENCE advertising: token=$token name=$displayName")
    }

    fun startOlaAdvertising(myTokenVal: String, targetToken: String) {
        olaActiveUntil = System.currentTimeMillis() + OLA_ADVERTISE_MS
        scheduleAdvertise(buildOlaPayload(myTokenVal, targetToken))
        Log.d(TAG, "OLA advertising → target=$targetToken")
    }

    fun revertToPresence(token: String, displayName: String) {
        olaActiveUntil = 0L
        scheduleAdvertise(buildPresencePayload(token, displayName))
        Log.d(TAG, "Reverted to PRESENCE advertising")
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        bleHandler.removeCallbacksAndMessages(null)
        pendingPayload = null
        val adv = adapter?.bluetoothLeAdvertiser ?: return
        activeAdvertiseCallback?.let {
            try { adv.stopAdvertising(it) } catch (e: Exception) { /* ignore */ }
        }
        activeAdvertiseCallback = null
        Log.d(TAG, "Advertising stopped")
    }

    /**
     * Stops current advertisement, waits ADVERTISE_START_DELAY_MS, then starts the new one.
     * Using a fresh AdvertiseCallback each time avoids ADVERTISE_FAILED_ALREADY_STARTED.
     */
    @SuppressLint("MissingPermission")
    private fun scheduleAdvertise(payload: ByteArray) {
        val adv = adapter?.bluetoothLeAdvertiser ?: run {
            Log.w(TAG, "BLE advertiser not available"); return
        }
        pendingPayload = payload

        // Cancel any pending start, stop the current one
        bleHandler.removeCallbacksAndMessages(null)
        activeAdvertiseCallback?.let {
            try { adv.stopAdvertising(it) } catch (e: Exception) { /* ignore */ }
        }
        activeAdvertiseCallback = null

        bleHandler.postDelayed({
            val p = pendingPayload ?: return@postDelayed
            val newCallback = makeAdvertiseCallback(p)
            activeAdvertiseCallback = newCallback
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(0)
                .build()
            val data = AdvertiseData.Builder()
                .addManufacturerData(MANUFACTURER_ID, p)
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .build()
            try {
                adv.startAdvertising(settings, data, newCallback)
            } catch (e: SecurityException) {
                Log.e(TAG, "startAdvertising: missing permission", e)
            } catch (e: Exception) {
                Log.e(TAG, "startAdvertising exception — retrying with BALANCED", e)
                retryWithBalanced(adv, p)
            }
        }, ADVERTISE_START_DELAY_MS)
    }

    @SuppressLint("MissingPermission")
    private fun retryWithBalanced(adv: android.bluetooth.le.BluetoothLeAdvertiser, payload: ByteArray) {
        val retryCallback = makeAdvertiseCallback(payload)
        activeAdvertiseCallback = retryCallback
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .setTimeout(0)
            .build()
        val data = AdvertiseData.Builder()
            .addManufacturerData(MANUFACTURER_ID, payload)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()
        try {
            adv.startAdvertising(settings, data, retryCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Retry also failed", e)
        }
    }

    private fun makeAdvertiseCallback(payload: ByteArray) = object : AdvertiseCallback() {
        override fun onStartSuccess(s: AdvertiseSettings) {
            Log.d(TAG, "Advertising started OK (${payload.size} bytes)")
        }
        override fun onStartFailure(errorCode: Int) {
            val reason = when (errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE       -> "DATA_TOO_LARGE"
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "TOO_MANY_ADVERTISERS"
                ADVERTISE_FAILED_ALREADY_STARTED      -> "ALREADY_STARTED"
                ADVERTISE_FAILED_INTERNAL_ERROR       -> "INTERNAL_ERROR"
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED  -> "FEATURE_UNSUPPORTED"
                else                                  -> "UNKNOWN($errorCode)"
            }
            Log.e(TAG, "Advertising FAILED: $reason")
            // Retry once more after a longer delay for transient errors
            if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR || errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                bleHandler.postDelayed({ scheduleAdvertise(payload) }, 2000)
            }
        }
    }

    // ------------------------------------------------------------------
    // Scanning
    // ------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    fun startScanning() {
        val sc = adapter?.bluetoothLeScanner ?: run {
            Log.w(TAG, "BLE scanner not available"); return
        }
        // No filter — filter by manufacturer ID in callback for maximum OEM compatibility.
        // Some Android OEM BLE stacks have bugs with manufacturer-data scan filters.
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()
        try {
            sc.startScan(emptyList(), settings, scanCallback)
            Log.d(TAG, "Scanning started")
        } catch (e: SecurityException) {
            Log.e(TAG, "startScan: missing permission", e)
        } catch (e: Exception) {
            Log.e(TAG, "startScan exception", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try { adapter?.bluetoothLeScanner?.stopScan(scanCallback) } catch (e: Exception) { /* ignore */ }
    }

    fun stop() {
        stopAdvertising()
        stopScanning()
    }

    fun evictStale(ttlMs: Long = DEVICE_TTL_MS) {
        val cutoff = System.currentTimeMillis() - ttlMs
        val stale = lastSeen.entries.filter { it.value < cutoff }.map { it.key }
        if (stale.isEmpty()) return
        stale.forEach { lastSeen.remove(it) }
        _nearbyTokens.update { current -> current - stale.toSet() }
        Log.d(TAG, "Evicted stale: $stale")
    }

    // ------------------------------------------------------------------
    // Payload builders
    // ------------------------------------------------------------------

    private fun buildPresencePayload(token: String, name: String): ByteArray {
        val nameBytes = name.toByteArray(Charsets.UTF_8).let { it.copyOf(minOf(it.size, NAME_MAX_BYTES)) }
        return byteArrayOf(MAGIC) + hexToBytes(token) + byteArrayOf(TYPE_PRESENCE) +
                byteArrayOf(nameBytes.size.toByte()) + nameBytes
    }

    private fun buildOlaPayload(myTokenVal: String, targetToken: String): ByteArray =
        byteArrayOf(MAGIC) + hexToBytes(myTokenVal) + byteArrayOf(TYPE_OLA) + hexToBytes(targetToken)

    // ------------------------------------------------------------------
    // Scan callback
    // ------------------------------------------------------------------

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val raw = result.scanRecord?.getManufacturerSpecificData(MANUFACTURER_ID) ?: return
            if (raw.size < 10 || raw[0] != MAGIC) return

            val senderToken = bytesToHex(raw.copyOfRange(1, 9))
            when (raw[9]) {
                TYPE_PRESENCE -> handlePresence(senderToken, raw)
                TYPE_OLA      -> handleOla(senderToken, raw)
            }
        }

        private fun handlePresence(senderToken: String, raw: ByteArray) {
            val nameLen = raw[10].toInt().and(0xFF).coerceAtMost(raw.size - 11)
            val name = if (nameLen > 0) String(raw.copyOfRange(11, 11 + nameLen), Charsets.UTF_8) else ""
            lastSeen[senderToken] = System.currentTimeMillis()
            _nearbyTokens.update { it + (senderToken to name) }
        }

        private fun handleOla(senderToken: String, raw: ByteArray) {
            if (raw.size < 18) return
            val targetToken = bytesToHex(raw.copyOfRange(10, 18))
            if (myToken.isEmpty() || targetToken != myToken) return
            val senderName = _nearbyTokens.value[senderToken] ?: ""
            Log.d(TAG, "OLA received from $senderToken ($senderName)")
            onOlaReceived?.invoke(senderToken, senderName)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
        }
    }

    companion object {
        fun hexToBytes(hex: String): ByteArray =
            ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

        fun bytesToHex(bytes: ByteArray): String =
            bytes.joinToString("") { "%02x".format(it) }
    }
}
