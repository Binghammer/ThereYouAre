package com.chadbingham.thereyouare

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.chadbingham.thereyouare.ui.theme.ThereYouAreTheme
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var appSettingsLauncher: ActivityResultLauncher<String>
    private lateinit var authLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //register for activity results
        permissionLauncher = registerForActivityResult(RequestMultiplePermissions()) { result ->
            viewModel.onPermissionResult(result)
        }

        appSettingsLauncher = registerForActivityResult(RequestPermission()) { result ->
            viewModel.onAppSettingsResult(result)
        }

        authLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
            viewModel.onSignInResult(result)
        }

        setContent {
            ThereYouAreTheme {
                MainScreen(viewModel)
            }
        }
    }

    @Composable
    fun MainScreen(viewModel: MainViewModel) {
        val state = viewModel.state.collectAsState().value
        val permissionRequest = remember { viewModel.permissionRequest }
        val signInRequest = remember { viewModel.signInRequest }
        val appSettingsRequest = remember { viewModel.appSettings }

        LaunchedEffect(key1 = permissionRequest) {
            permissionRequest.collect {
                Timber.d("permissionRequest")
                permissionLauncher.launch(it)
            }
        }

        LaunchedEffect(key1 = signInRequest) {
            signInRequest.collect {
                Timber.d("signInRequest")
                authLauncher.launch(it)
            }
        }


        LaunchedEffect(key1 = appSettingsRequest) {
            Timber.d("LaunchedEffect for appSettingsRequest started")
            Timber.d("About to call appSettingsRequest.collect")
            appSettingsRequest.collect {
                Timber.d("appSettingsRequest")
                openAppSettings()
            }
            Timber.d("Finished collecting")
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                if(state.signInVisible) {
                    Button(content = {
                        Text(text = "Sign In")
                    },
                        onClick = {
                            viewModel.signIn()
                        })
                }
            }
        }

        if (state.showRationale) {
            PermissionRationaleDialog(
                onDismiss = { viewModel.onDialogDismissed() },
                onRequestPermission = { viewModel.onDialogConfirm() },
            )
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun requestForegroundPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            ),
            FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        const val FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
    }
}

