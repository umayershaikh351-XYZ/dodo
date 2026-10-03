package com.example

import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.example.ui.navigation.CipherLockApp
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.CipherLockTheme
import com.example.ui.viewmodel.CipherLockViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CipherLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CipherLockTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundPrimary)
                        .safeDrawingPadding() // Respects camera notch, status bar, navigation bar
                ) {
                    CipherLockApp(
                        viewModel = viewModel,
                        onTriggerBiometric = { onSuccess ->
                            triggerBiometricAuthentication(onSuccess)
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        // Aggressive One UI background auto-lock & immediate clipboard clearing
        viewModel.onAppBackgrounded()
    }

    private fun triggerBiometricAuthentication(onSuccess: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val cancellationSignal = CancellationSignal()
                val prompt = android.hardware.biometrics.BiometricPrompt.Builder(this)
                    .setTitle("CIPHERLOCK BIOMETRIC AUTH")
                    .setSubtitle("Confirm hardware biometric authentication")
                    .setDescription("Samsung Knox & Android Keystore Verified")
                    .setNegativeButton("USE PIN", mainExecutor) { _, _ -> }
                    .build()

                prompt.authenticate(
                    cancellationSignal,
                    mainExecutor,
                    object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }
                    }
                )
            } catch (_: Exception) {
                // Biometrics unavailable or not enrolled
            }
        }
    }
}
