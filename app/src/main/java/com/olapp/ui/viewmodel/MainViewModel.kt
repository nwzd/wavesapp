package com.olapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olapp.data.preferences.AppPreferences
import com.olapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

private const val TAG = "MainViewModel"
private const val RESOLVE_TIMEOUT_MS = 5_000L

sealed class MainUiState {
    data object Loading : MainUiState()
    data object NeedsTermsAcceptance : MainUiState()
    data object NeedsSetup : MainUiState()
    data object Ready : MainUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = safeResolveState()
        }
    }

    fun setTermsAccepted() {
        viewModelScope.launch {
            appPreferences.setTermsAccepted(true)
            _uiState.value = withContext(Dispatchers.IO) { safeResolveState() }
        }
    }

    fun refreshState() {
        viewModelScope.launch {
            appPreferences.setSetupComplete(true)
            _uiState.value = MainUiState.Ready
        }
    }

    private suspend fun safeResolveState(): MainUiState {
        return try {
            withTimeout(RESOLVE_TIMEOUT_MS) { resolveState() }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "resolveState timed out — defaulting to NeedsTermsAcceptance")
            MainUiState.NeedsTermsAcceptance
        } catch (e: Exception) {
            Log.e(TAG, "resolveState failed", e)
            MainUiState.NeedsSetup
        }
    }

    private suspend fun resolveState(): MainUiState {
        val termsAccepted = appPreferences.isTermsAccepted.first()
        if (!termsAccepted) return MainUiState.NeedsTermsAcceptance
        val setupComplete = appPreferences.isSetupComplete.first()
        val profile = userRepository.getMyProfile()
        return if (!setupComplete || profile == null) MainUiState.NeedsSetup else MainUiState.Ready
    }
}
