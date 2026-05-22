package com.olapp.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
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
private const val SCAN_RESTART_DELAY_MS = 3_000L
private const val MAX_ADV_RETRIES = 3

@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val adapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val bleHandler = Handler(Looper.getMainLooper())

    // Coded PHY (BT5) extends range from ~30m to ~300m. Falls back to legacy if unsupported.
    private val supportsCodedPhy: Boolean by lazy {
        adapter?.isLeCodedPhySupported == true && adapter?.isLeExtendedAdvertisingSupported == true
    }

    private val _nearbyTokens = MutableStateFlow<Map<String, String>>(emptyMap())
    val nearbyTokens: StateFlow<Map<String, String>> = _nearbyTokens.asStateFlow()

    private val lastSeen = ConcurrentHashMap<String, Long>()

    var myToken: String = ""
    var onOlaReceived: ((senderToken: String, senderDisplayName: String) -> Unit)? = null

    private var olaActiveUntil: Long = 0L
    fun isOlaActive(): Boolean = System.currentTimeMillis() < olaActiveUntil

    fun isTokenInRange(token: String): Boolean = _nearbyTokens.value.containsKey(token)

    // Legacy advertising state (used when Coded PHY is unavailable)
    @Volatile private var activeLegacyCallback: AdvertiseCallback? = null
    // Extended advertising state (Coded PHY / BT5)
    @Volatile private var activeAdvSet: AdvertisingSet? = null
    @Volatile private var pendingPayload: ByteArray? = null
    private var advRetryCount = 0

    private var scanRunning = false

    // ------------------------------------------------------------------
    // Advertising — public API
    // ------------------------------------------------------------------

    fun startPresenceAdvertising(token: String, displayName: String) {
        if (isOlaActive()) return
        scheduleAdvertise(buildPresencePayload(token, displayName))
        Log.d(TAG, "PRESENCE advertising: coded=$supportsCodedPhy token=$token")
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
        advRetryCount = 0
        val adv = adapter?.bluetoothLeAdvertiser
        activeLegacyCallback?.let { cb ->
            try { adv?.stopAdvertising(cb) } catch (e: Exception) { /* ignore */ }
            activeLegacyCallback = null
        }
        activeAdvSet?.let {
            try { adv?.stopAdvertisingSet(extAdvCallback) } catch (e: Exception) { /* ignore */ }
            activeAdvSet = null
        }
        Log.d(TAG, "Advertising stopped")
    }

    // ------------------------------------------------------------------
    // Advertising — internal
    // ------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun scheduleAdvertise(payload: ByteArray) {
        val adv = adapter?.bluetoothLeAdvertiser ?: run {
            Log.w(TAG, "BLE advertiser not available"); return
        }
        pendingPayload = payload
        advRetryCount = 0

        bleHandler.removeCallbacksAndMessages(null)
        // Stop any active advertisement before starting a new one
        activeLegacyCallback?.let { try { adv.stopAdvertising(it) } catch (e: Exception) { } }
        activeLegacyCallback = null
        activeAdvSet?.let { try { adv.stopAdvertisingSet(extAdvCallback) } catch (e: Exception) { } }
        activeAdvSet = null

        bleHandler.postDelayed({
            val p = pendingPayload ?: return@postDelayed
            val freshAdv = adapter?.bluetoothLeAdvertiser ?: run {
                Log.w(TAG, "Advertiser gone by the time handler fired"); return@postDelayed
            }
            if (supportsCodedPhy) startExtendedAdvertising(freshAdv, p)
            else startLegacyAdvertising(freshAdv, p)
        }, ADVERTISE_START_DELAY_MS)
    }

    @SuppressLint("MissingPermission")
    private fun startExtendedAdvertising(adv: android.bluetooth.le.BluetoothLeAdvertiser, payload: ByteArray) {
        val params = AdvertisingSetParameters.Builder()
            .setLegacyMode(false)
            .setConnectable(false)
            .setPrimaryPhy(BluetoothDevice.PHY_LE_CODED)
            .setSecondaryPhy(BluetoothDevice.PHY_LE_CODED)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
            .build()
        val data = AdvertiseData.Builder()
            .addManufacturerData(MANUFACTURER_ID, payload)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()
        try {
            adv.startAdvertisingSet(params, data, null, null, null, extAdvCallback)
        } catch (e: Exception) {
            Log.e(TAG, "startAdvertisingSet failed — falling back to legacy", e)
            startLegacyAdvertising(adv, payload)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLegacyAdvertising(adv: android.bluetooth.le.BluetoothLeAdvertiser, payload: ByteArray) {
        val cb = makeLegacyCallback(payload)
        activeLegacyCallback = cb
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0)
            .build()
        val data = AdvertiseData.Builder()
            .addManufacturerData(MANUFACTURER_ID, payload)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()
        try {
            adv.startAdvertising(settings, data, cb)
        } catch (e: SecurityException) {
            Log.e(TAG, "startAdvertising: missing permission", e)
            activeLegacyCallback = null
        } catch (e: Exception) {
            Log.e(TAG, "startAdvertising exception — retrying BALANCED in 2s", e)
            activeLegacyCallback = null
            scheduleRetry(payload)
        }
    }

    @SuppressLint("MissingPermission")
    private fun scheduleRetry(payload: ByteArray) {
        if (advRetryCount >= MAX_ADV_RETRIES) {
            Log.e(TAG, "Advertising giving up after $MAX_ADV_RETRIES retries")
            advRetryCount = 0
            return
        }
        advRetryCount++
        bleHandler.postDelayed({
            val adv = adapter?.bluetoothLeAdvertiser ?: return@postDelayed
            Log.d(TAG, "Retry advertising attempt $advRetryCount")
            val cb = makeLegacyCallback(payload)
            activeLegacyCallback = cb
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
                adv.startAdvertising(settings, data, cb)
            } catch (e: Exception) {
                Log.e(TAG, "Retry $advRetryCount failed", e)
                activeLegacyCallback = null
                scheduleRetry(payload)
            }
        }, 2_000L * advRetryCount)
    }

    private val extAdvCallback = object : AdvertisingSetCallback() {
        override fun onAdvertisingSetStarted(set: AdvertisingSet?, txPower: Int, status: Int) {
            if (status == ADVERTISE_SUCCESS) {
                activeAdvSet = set
                Log.d(TAG, "Extended (Coded PHY) advertising started, txPower=$txPower")
            } else {
                Log.e(TAG, "Extended advertising failed status=$status — falling back to legacy")
                activeAdvSet = null
                pendingPayload?.let { p ->
                    bleHandler.post {
                        val adv = adapter?.bluetoothLeAdvertiser ?: return@post
                        startLegacyAdvertising(adv, p)
                    }
                }
            }
        }
        override fun onAdvertisingSetStopped(set: AdvertisingSet?) {
            if (activeAdvSet == set) activeAdvSet = null
        }
    }

    private fun makeLegacyCallback(payload: ByteArray) = object : AdvertiseCallback() {
        override fun onStartSuccess(s: AdvertiseSettings) {
            advRetryCount = 0
            Log.d(TAG, "Legacy advertising started OK (${payload.size} bytes)")
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
            Log.e(TAG, "Legacy advertising FAILED: $reason")
            activeLegacyCallback = null
            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE || errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) return
            scheduleRetry(payload)
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
        // Always stop first — prevents duplicate scan registrations that exhaust BLE scan slots
        if (scanRunning) {
            try { sc.stopScan(scanCallback) } catch (e: Exception) { }
            scanRunning = false
        }
        val settingsBuilder = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        // Enable extended advertising scan (picks up Coded PHY advertisements)
        if (supportsCodedPhy) {
            settingsBuilder.setLegacy(false)
            settingsBuilder.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
            Log.d(TAG, "Extended scanning enabled (Coded PHY)")
        }
        try {
            sc.startScan(emptyList(), settingsBuilder.build(), scanCallback)
            scanRunning = true
            Log.d(TAG, "Scanning started")
        } catch (e: SecurityException) {
            Log.e(TAG, "startScan: missing permission", e)
        } catch (e: Exception) {
            Log.e(TAG, "startScan exception", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        scanRunning = false
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
            Log.e(TAG, "Scan failed: $errorCode — restarting in ${SCAN_RESTART_DELAY_MS}ms")
            scanRunning = false
            // Restart scan after a short delay — common on OEM stacks when BT state is recovering
            bleHandler.postDelayed({ startScanning() }, SCAN_RESTART_DELAY_MS)
        }
    }

    companion object {
        fun hexToBytes(hex: String): ByteArray =
            ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

        fun bytesToHex(bytes: ByteArray): String =
            bytes.joinToString("") { "%02x".format(it) }
    }
}
