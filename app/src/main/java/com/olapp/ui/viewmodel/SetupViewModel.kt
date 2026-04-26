package com.olapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olapp.data.preferences.AppPreferences
import com.olapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Pre-filled values when editing an existing profile
    private val _existingDisplayName = MutableStateFlow("")
    val existingDisplayName: StateFlow<String> = _existingDisplayName.asStateFlow()

    private val _existingContactInfo = MutableStateFlow("")
    val existingContactInfo: StateFlow<String> = _existingContactInfo.asStateFlow()

    private val _existingDescription = MutableStateFlow("")
    val existingDescription: StateFlow<String> = _existingDescription.asStateFlow()

    private val _existingPhotoPath = MutableStateFlow<String?>(null)
    val existingPhotoPath: StateFlow<String?> = _existingPhotoPath.asStateFlow()

    private val _existingDiscoverable = MutableStateFlow(true)
    val existingDiscoverable: StateFlow<Boolean> = _existingDiscoverable.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userRepository.getMyProfile() ?: return@launch
            _existingDisplayName.value = profile.displayName
            _existingContactInfo.value = profile.contactInfo
            _existingDescription.value = profile.description
            _existingPhotoPath.value = profile.photoUrl.ifBlank { null }
            _existingDiscoverable.value = profile.discoveryEnabled
        }
    }

    fun save(
        displayName: String,
        contactInfo: String,
        description: String,
        photoUri: Uri?,
        existingPhotoPath: String?,
        discoveryEnabled: Boolean
    ) {
        if (displayName.isBlank()) { _error.value = "Name can't be empty"; return }

        viewModelScope.launch {
            _isSaving.value = true
            runCatching {
                val photoPath = when {
                    photoUri != null -> copyPhotoToInternalStorage(photoUri)
                    existingPhotoPath != null -> existingPhotoPath
                    else -> ""
                }
                userRepository.saveProfile(displayName, contactInfo, photoPath, discoveryEnabled, description)
            }.onSuccess {
                appPreferences.setSetupComplete(true)
                _saved.value = true
            }.onFailure {
                _error.value = it.message ?: "Save failed"
            }
            _isSaving.value = false
        }
    }

    private suspend fun copyPhotoToInternalStorage(uri: Uri): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(dir, "profile.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    }

    fun clearError() { _error.value = null }
}
