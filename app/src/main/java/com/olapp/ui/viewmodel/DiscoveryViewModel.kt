package com.olapp.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.olapp.data.repository.UserRepository
import com.olapp.nearby.NearbyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

enum class WaveStatus { NONE, WAVE_SENT, MATCHED }

data class NearbyDevice(
    val bleToken: String,
    val endpointId: String,
    val displayName: String,
    val description: String = "",
    val photoPath: String? = null,
    val status: WaveStatus = WaveStatus.NONE,
    val isPending: Boolean = false
)

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val nearbyManager: NearbyManager,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _nearbyDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<NearbyDevice>> = _nearbyDevices.asStateFlow()

    private val _discoveryEnabled = MutableStateFlow(true)
    val discoveryEnabled: StateFlow<Boolean> = _discoveryEnabled.asStateFlow()

    init {
        observeNearby()
        loadDiscoveryState()
    }

    private fun observeNearby() {
        viewModelScope.launch {
            combine(
                nearbyManager.peers,
                nearbyManager.pendingEndpoints,
                userRepository.observeSentOlas(),
                userRepository.observeMatches()
            ) { peers, pending, sent, matches ->
                val sentTokens = sent.map { it.receiverBleToken }.toSet()
                val matchedTokens = matches.map { it.otherBleToken }.toSet()
                val devices = mutableMapOf<String, NearbyDevice>()

                pending.values.forEach { pp ->
                    val status = when {
                        pp.bleToken in matchedTokens -> WaveStatus.MATCHED
                        pp.bleToken in sentTokens    -> WaveStatus.WAVE_SENT
                        else                         -> WaveStatus.NONE
                    }
                    devices[pp.bleToken] = NearbyDevice(
                        bleToken = pp.bleToken,
                        endpointId = "",
                        displayName = pp.displayName.ifEmpty { "Someone nearby" },
                        status = status,
                        isPending = true
                    )
                }

                peers.values.forEach { peer ->
                    val status = when {
                        peer.bleToken in matchedTokens -> WaveStatus.MATCHED
                        peer.bleToken in sentTokens    -> WaveStatus.WAVE_SENT
                        else                           -> WaveStatus.NONE
                    }
                    devices[peer.bleToken] = NearbyDevice(
                        bleToken = peer.bleToken,
                        endpointId = peer.endpointId,
                        displayName = peer.displayName.ifEmpty { "Someone nearby" },
                        description = peer.description,
                        photoPath = peer.photoPath,
                        status = status,
                        isPending = false
                    )
                }

                devices.values.filter { it.status != WaveStatus.MATCHED }.toList()
            }.collect { _nearbyDevices.value = it }
        }
    }

    fun sendOla(bleToken: String) {
        viewModelScope.launch {
            val device = _nearbyDevices.value.find { it.bleToken == bleToken }
            if (device == null || device.status != WaveStatus.NONE) return@launch

            val profile = userRepository.getMyProfile() ?: return@launch
            if (bleToken == profile.bleToken) return@launch

            userRepository.saveSentOla(
                receiverBleToken = bleToken,
                receiverDisplayName = device.displayName,
                receiverPhotoUrl = device.photoPath ?: "",
                latitude = null,
                longitude = null
            )

            nearbyManager.endpointIdForToken(bleToken)
                ?.let { nearbyManager.sendOla(it) }

            val received = userRepository.getLatestReceivedOlaFrom(bleToken)
            if (received != null) {
                val peer = nearbyManager.peers.value.values.find { it.bleToken == bleToken }
                val location = getLastLocation()
                val resolvedName = peer?.displayName?.takeIf { it.isNotEmpty() }
                    ?: received.senderDisplayName.ifEmpty { device.displayName }
                val resolvedPhoto = peer?.photoPath?.takeIf { it.isNotEmpty() }
                    ?: received.senderPhotoUrl
                userRepository.createMatch(
                    otherBleToken = bleToken,
                    otherDisplayName = resolvedName,
                    otherPhotoUrl = resolvedPhoto,
                    otherContactInfo = received.senderContactInfo.ifEmpty { peer?.contactInfo ?: "" },
                    latitude = location?.first,
                    longitude = location?.second
                )
                if (location != null) {
                    nearbyManager.endpointIdForToken(bleToken)
                        ?.let { nearbyManager.sendMatchLocation(it, location.first, location.second) }
                }
            }
        }
    }

    fun requestHdPhoto(bleToken: String) {
        nearbyManager.endpointIdForToken(bleToken)
            ?.let { nearbyManager.requestHdPhoto(it) }
    }

    fun toggleDiscovery(enabled: Boolean) {
        viewModelScope.launch {
            userRepository.updateDiscovery(enabled)
            _discoveryEnabled.value = enabled
            val profile = userRepository.getMyProfile() ?: return@launch
            if (enabled) nearbyManager.startDiscoveryAndAdvertising(profile)
            else nearbyManager.stop()
        }
    }

    private fun loadDiscoveryState() {
        viewModelScope.launch {
            _discoveryEnabled.value = userRepository.getMyProfile()?.discoveryEnabled ?: true
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { loc -> cont.resume(if (loc != null) loc.latitude to loc.longitude else null) }
                    .addOnFailureListener { cont.resume(null) }
            } catch (e: SecurityException) { cont.resume(null) }
        }
}
