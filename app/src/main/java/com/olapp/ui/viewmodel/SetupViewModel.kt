package com.olapp.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olapp.data.preferences.AppPreferences
import com.olapp.data.repository.UserRepository
import com.olapp.util.PhotoValidationResult
import com.olapp.util.PhotoValidator
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

    private val _existingDisplayName = MutableStateFlow("")
    val existingDisplayName: StateFlow<String> = _existingDisplayName.asStateFlow()

    private val _existingContactInfo = MutableStateFlow("")
    val existingContactInfo: StateFlow<String> = _existingContactInfo.asStateFlow()

    private val _existingDescription = MutableStateFlow("")
    val existingDescription: StateFlow<String> = _existingDescription.asStateFlow()

    private val _existingPhotoPath = MutableStateFlow<String?>(null)
    val existingPhotoPath: StateFlow<String?> = _existingPhotoPath.asStateFlow()

    private val _existingDiscoverable = MutableStateFlow(false)
    val existingDiscoverable: StateFlow<Boolean> = _existingDiscoverable.asStateFlow()

    private val _validatedPhotoUri = MutableStateFlow<Uri?>(null)
    val validatedPhotoUri: StateFlow<Uri?> = _validatedPhotoUri.asStateFlow()

    private val _photoValidating = MutableStateFlow(false)
    val photoValidating: StateFlow<Boolean> = _photoValidating.asStateFlow()

    private val _photoError = MutableStateFlow<String?>(null)
    val photoError: StateFlow<String?> = _photoError.asStateFlow()

    private val _photoIsSelfie = MutableStateFlow(false)
    val photoIsSelfie: StateFlow<Boolean> = _photoIsSelfie.asStateFlow()

    private val _isFirstSetup = MutableStateFlow(false)
    val isFirstSetup: StateFlow<Boolean> = _isFirstSetup.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userRepository.getMyProfile()
            _isFirstSetup.value = (profile == null)
            if (profile == null) return@launch
            _existingDisplayName.value = profile.displayName
            _existingContactInfo.value = profile.contactInfo
            _existingDescription.value = profile.description
            _existingPhotoPath.value = profile.photoUrl.ifBlank { null }
            _existingDiscoverable.value = profile.discoveryEnabled
        }
    }

    fun createTempPhotoUri(): Uri {
        val file = File(context.cacheDir, "selfie_temp.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun onPhotoSelected(uri: Uri, isSelfie: Boolean = false) {
        viewModelScope.launch {
            _photoValidating.value = true
            _photoError.value = null
            val result = withContext(Dispatchers.Default) {
                PhotoValidator.validate(context, uri)
            }
            when (result) {
                is PhotoValidationResult.Valid -> {
                    _validatedPhotoUri.value = uri
                    _photoIsSelfie.value = isSelfie
                }
                is PhotoValidationResult.NoFace ->
                    _photoError.value = "No face detected — please use a clear photo of your face."
                is PhotoValidationResult.TooManyFaces ->
                    _photoError.value = "Multiple faces detected — please use a solo photo."
                is PhotoValidationResult.Inappropriate ->
                    _photoError.value = "Photo appears inappropriate. Please use a suitable photo."
            }
            _photoValidating.value = false
        }
    }

    fun save(
        displayName: String,
        contactInfo: String,
        description: String,
        discoveryEnabled: Boolean,
        isOver18: Boolean = false
    ) {
        if (displayName.isBlank()) { _error.value = "Name can't be empty"; return }
        if (_isFirstSetup.value && !isOver18) {
            _error.value = "You must confirm you are 18 or older to use Wave & Vibe."
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            runCatching {
                val photoPath = when {
                    _validatedPhotoUri.value != null ->
                        copyPhotoToInternalStorage(_validatedPhotoUri.value!!)
                    _existingPhotoPath.value != null -> _existingPhotoPath.value!!
                    else -> ""
                }
                val selfie = if (_validatedPhotoUri.value != null) _photoIsSelfie.value
                             else false
                userRepository.saveProfile(displayName, contactInfo, photoPath, discoveryEnabled, description, selfie)
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
    fun clearPhotoError() { _photoError.value = null }
}
