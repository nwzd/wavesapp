package com.olapp.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.olapp.data.model.Match
import com.olapp.data.model.ReceivedOla
import com.olapp.data.model.SentOla
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

private const val TAG = "OlaViewModel"

@HiltViewModel
class OlaViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val nearbyManager: NearbyManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _receivedOlas = MutableStateFlow<List<ReceivedOla>>(emptyList())
    val receivedOlas: StateFlow<List<ReceivedOla>> = _receivedOlas.asStateFlow()

    private val _sentOlas = MutableStateFlow<List<SentOla>>(emptyList())
    val sentOlas: StateFlow<List<SentOla>> = _sentOlas.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _matchSnackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _matchSnackbar.asStateFlow()

    init {
        viewModelScope.launch {
            try { userRepository.observeMatches().collect { _matches.value = it } }
            catch (e: Exception) { Log.e(TAG, "observeMatches failed", e) }
        }
        viewModelScope.launch {
            try {
                combine(
                    userRepository.observeReceivedOlas(),
                    userRepository.observeMatches()
                ) { received, matches ->
                    val matchedTokens = matches.map { it.otherBleToken }.toSet()
                    received.filter { it.senderBleToken !in matchedTokens }
                }.collect { _receivedOlas.value = it }
            } catch (e: Exception) { Log.e(TAG, "observeReceivedOlas failed", e) }
        }
        viewModelScope.launch {
            try {
                combine(
                    userRepository.observeSentOlas(),
                    userRepository.observeMatches()
                ) { sent, matches ->
                    val matchedTokens = matches.map { it.otherBleToken }.toSet()
                    sent.filter { it.receiverBleToken !in matchedTokens }
                }.collect { _sentOlas.value = it }
            } catch (e: Exception) { Log.e(TAG, "observeSentOlas failed", e) }
        }
    }

    fun respondToOla(senderBleToken: String) {
        viewModelScope.launch {
            val receivedOla = userRepository.getLatestReceivedOlaFrom(senderBleToken) ?: return@launch

            userRepository.saveSentOla(
                receiverBleToken = senderBleToken,
                receiverDisplayName = receivedOla.senderDisplayName,
                receiverPhotoUrl = receivedOla.senderPhotoUrl,
                latitude = null,
                longitude = null
            )

            val endpointId = nearbyManager.endpointIdForToken(senderBleToken)
            if (endpointId != null) nearbyManager.sendOla(endpointId)
            else Log.w(TAG, "respondToOla: $senderBleToken not currently connected")

            val location = getLastLocation()
            val peer = nearbyManager.peers.value.values.find { it.bleToken == senderBleToken }
            val resolvedName = peer?.displayName?.takeIf { it.isNotEmpty() }
                ?: receivedOla.senderDisplayName
            val resolvedPhoto = peer?.photoPath?.takeIf { it.isNotEmpty() }
                ?: receivedOla.senderPhotoUrl
            userRepository.createMatch(
                otherBleToken = senderBleToken,
                otherDisplayName = resolvedName,
                otherPhotoUrl = resolvedPhoto,
                otherContactInfo = receivedOla.senderContactInfo.ifEmpty { peer?.contactInfo ?: "" },
                latitude = location?.first,
                longitude = location?.second
            )
            if (location != null) {
                nearbyManager.endpointIdForToken(senderBleToken)
                    ?.let { nearbyManager.sendMatchLocation(it, location.first, location.second) }
            }
        }
    }

    fun deleteMatch(matchId: String, otherBleToken: String) {
        viewModelScope.launch {
            userRepository.deleteMatch(matchId, otherBleToken)
        }
    }

    fun clearSnackbar() { _matchSnackbar.value = null }

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
