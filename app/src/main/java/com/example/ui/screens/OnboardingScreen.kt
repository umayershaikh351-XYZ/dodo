package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScanlineOverlay
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalFooter
import com.example.ui.components.TerminalPanel
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    onProceedToSetup: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        ScanlineOverlay()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "> CIPHERLOCK SECURITY PROTOCOL",
                    fontFamily = TerminalFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen,
                    letterSpacing = 0.08.sp
                )
                Text(
                    text = "DEFENSIVE DEVICE INTELLIGENCE & VAULT",
                    fontFamily = TerminalFontFamily,
                    fontSize = 11.sp,
                    color = MatrixGreenDim,
                    letterSpacing = 0.05.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                TerminalPanel(
                    hasLeftAccentBar = true,
                    hasAngledCut = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "CORE ARCHITECTURE MANDATES",
                            fontFamily = TerminalFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CipherLock operates under strict zero-trust operational criteria. No network sockets. No server dependencies. 100% on-device cryptography.",
                            fontFamily = TerminalFontFamily,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val protocols = listOf(
                    Triple(
                        "01 // ZERO TRANSMISSION",
                        "App manifest has ZERO internet permission. Runs seamlessly in airplane mode. No data ever leaves this hardware.",
                        MatrixGreen
                    ),
                    Triple(
                        "02 // HARDWARE KEYSTORE VAULT",
                        "All credentials and notes are sealed with AES-256-GCM. Encryption keys reside in the Android Secure Enclave / TPM.",
                        StatusInfo
                    ),
                    Triple(
                        "03 // PASSIVE INTEL ENGINE",
                        "Automatic telemetry collection across hardware, One UI 8.5, and Knox integrity without requiring intrusive permissions.",
                        MatrixGreen
                    ),
                    Triple(
                        "04 // SECURITY SCORING",
                        "Holistic vulnerability audit comparing your device parameters against 12 hardening vectors with direct remediation.",
                        MatrixGreen
                    )
                )

                protocols.forEach { (title, desc, color) ->
                    TerminalPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ">",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 12.sp,
                                    color = MatrixGreenDim,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                fontFamily = TerminalFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
                TerminalButton(
                    text = "[ INITIALIZE SECURITY ENVIRONMENT ]",
                    onClick = onProceedToSetup,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "onboarding_continue_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                TerminalFooter()
            }
        }
    }
}
