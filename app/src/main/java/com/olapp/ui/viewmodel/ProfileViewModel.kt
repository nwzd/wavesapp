package com.olapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olapp.ble.BleForegroundService
import com.olapp.data.local.entity.BlockedUserEntity
import com.olapp.data.model.UserProfile
import com.olapp.data.preferences.AppPreferences
import com.olapp.data.repository.UserRepository
import com.olapp.nearby.NearbyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences,
    private val nearbyManager: NearbyManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userRepository.observeMyProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isDarkMode: StateFlow<Boolean> = appPreferences.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val blockedUsers: StateFlow<List<BlockedUserEntity>> = userRepository.observeBlockedUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setDarkMode(enabled) }
    }

    fun unblockUser(token: String) {
        viewModelScope.launch {
            userRepository.unblockUser(token)
            nearbyManager.removeBlockedToken(token)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            userRepository.clearAll()
            BleForegroundService.clearState(context)
        }
    }
}
