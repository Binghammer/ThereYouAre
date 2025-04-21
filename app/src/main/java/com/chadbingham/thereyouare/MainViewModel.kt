package com.chadbingham.thereyouare

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chadbingham.thereyouare.data.ForegroundLocationService
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val permissionModel: PermissionModel,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(MainViewState())
    val state: StateFlow<MainViewState> = _state.asStateFlow()

    private val _permissionRequest = MutableSharedFlow<Array<String>>()
    val permissionRequest: SharedFlow<Array<String>> = _permissionRequest.asSharedFlow()

    private val permissions: Array<String> = arrayOf(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION,
    )

    private val _signInRequest = MutableSharedFlow<Intent>()
    val signInRequest: SharedFlow<Intent> = _signInRequest.asSharedFlow()

    private val _appSettings = MutableSharedFlow<String>()
    val appSettings: SharedFlow<String> = _appSettings.asSharedFlow()

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
    )

    init {
        if (FirebaseAuth.getInstance().currentUser == null) {
            Timber.d("FirebaseUser not auth'd")
            _state.value = _state.value.copy(signInVisible = true)

        } else {
            checkForPermissions()
        }
    }

    private fun checkForPermissions() {
        Timber.d("checkForPermissions")
        if (permissionModel.areLocationPermissionsGranted()) {
            if (permissionModel.areBackgroundPermissionsGranted()) {
                Timber.d("permissions already granted")
                startService()

            } else {
                Timber.d("requesting background permissions")
                viewModelScope.launch {
                    _appSettings.emit(ACCESS_BACKGROUND_LOCATION)
                }
            }
        } else if (permissionModel.shouldShowRationale()) {
            Timber.d("shouldShowRationale")
            _state.value = _state.value.copy(showRationale = true)
        } else {
            Timber.d("requesting permissions")
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        viewModelScope.launch {
            _permissionRequest.emit(permissions)
        }
    }

    private fun startService() {
        startForegroundService(
            getApplication(),
            Intent(getApplication(), ForegroundLocationService::class.java)
        )
    }

    fun onDialogDismissed() {
        _state.value = _state.value.copy(showRationale = false)
    }

    fun onDialogConfirm() {
        _state.value = _state.value.copy(showRationale = false)
        requestPermissions()
    }

    fun onPermissionResult(result: Map<String, Boolean>) {
        val allPermissionsGranted = result.values.all { it }

        if (allPermissionsGranted) {
            // permissions are granted, do the things that require permissions
            checkForPermissions()
        } else {
            permissionModel.permissionsDenied(result.filter { !it.value }.keys.toTypedArray())
            if (!permissionModel.shouldShowRationale()) {
                _state.value = _state.value.copy(showRationale = true)
            }
        }
    }

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            checkForPermissions()
            _state.value = _state.value.copy(signInVisible = false)
        } else {
            if (response != null) {
                Timber.e(response.error, "Sign-in error")
                // Check for specific error codes in response.error
            } else {
                Timber.w("Sign-in flow canceled by the user")
            }
        }
    }

    fun onAppSettingsResult(result: Boolean) {
        Timber.d("onAppSettingsResult: $result")
        if (result) {
            startService()
        } else {
            _state.value = _state.value.copy(showRationale = true)
        }
    }

    fun signIn() {
        viewModelScope.launch {
            _signInRequest.emit(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build()
            )
        }
    }
}

data class MainViewState(
    val signInVisible: Boolean = false,
    val showRationale: Boolean = false,
)
