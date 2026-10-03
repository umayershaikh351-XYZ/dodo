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
import com.example.ui.components.TerminalButtonType
import com.example.ui.components.TerminalFooter
import com.example.ui.components.TerminalPanel
import com.example.ui.components.TerminalStatusBar
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    val hexDumpSample = """
        00000000: 7f45 4c46 0201 0100 0000 0000 0000 0000  .ELF............
        00000010: 0300 b700 0100 0000 3014 0000 0000 0000  ........0.......
        00000020: 4000 0000 0000 0000 b831 0100 0000 0000  @.......1......
        00000030: 0000 0000 4000 3800 0900 4000 1f00 1e00  ....@.8...@.....
        00000040: 0600 0000 0400 0000 4000 0000 0000 0000  ........@.......
        00000050: 4000 0000 0000 0000 4000 0000 0000 0000  @.......@.......
        00000060: f801 0000 0000 0000 f801 0000 0000 0000  ................
        00000070: 0800 0000 0000 0000 0300 0000 0400 0000  ................
        00000080: 3802 0000 0000 0000 3802 0000 0000 0000  8.......8.......
        00000090: 1c00 0000 0000 0000 1c00 0000 0000 0000  ................
        000000a0: 0100 0000 0000 0000 0100 0000 0500 0000  ................
        000000b0: 0000 0000 0000 0000 0000 0000 0000 0000  ................
        000000c0: 2420 0100 0000 0000 2420 0100 0000 0000  $ ......$ ......
        000000d0: 0010 0000 0000 0000 0100 0000 0600 0000  ................
    """.trimIndent()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // Hex Dump Watermark in background at 8% opacity
        Text(
            text = hexDumpSample.repeat(3),
            fontFamily = TerminalFontFamily,
            fontSize = 9.sp,
            color = MatrixGreen.copy(alpha = 0.08f),
            lineHeight = 13.sp,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )

        ScanlineOverlay()

        Column(modifier = Modifier.fillMaxSize()) {
            TerminalStatusBar(score = 95, isLocked = false)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "> ABOUT CIPHERLOCK",
                            fontFamily = TerminalFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            text = "SECURITY AUDIT & HARDENED VAULT",
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            color = MatrixGreenDim
                        )
                    }
                    TerminalButton(
                        text = "[ BACK ]",
                        onClick = onNavigateBack,
                        type = TerminalButtonType.SECONDARY,
                        testTag = "about_back_btn"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TerminalPanel(
                    hasAngledCut = true,
                    hasLeftAccentBar = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "CIPHERLOCK v1.0",
                            fontFamily = TerminalFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "BUILT FOR SAMSUNG GALAXY A17 5G (SM-A176B/DS)",
                            fontFamily = TerminalFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ONE UI 8.5  |  ANDROID 16 (API 36)  |  KNOX 3.13",
                            fontFamily = TerminalFontFamily,
                            fontSize = 11.sp,
                            color = StatusInfo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "THE ZERO-TRUST COMMITMENT",
                            fontFamily = TerminalFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusOk
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "100% OFFLINE. NO SERVER. NO TRACKING.\n" +
                                    "CipherLock strictly excludes android.permission.INTERNET from its compiled manifest.\n\n" +
                                    "Your cryptographic keys never touch RAM in plain bytes longer than necessary for encryption. " +
                                    "All database records are stored in AES-256-GCM cipher blocks using Android Keystore keys generated directly on the device hardware.",
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "COMPLIANCE & PERMISSIONS",
                            fontFamily = TerminalFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• USE_BIOMETRIC: Local fingerprint/face scanner authentication.\n" +
                                    "• ACCESS_NETWORK_STATE: Read-only network interface status.\n" +
                                    "• ACCESS_WIFI_STATE: Read-only wireless encryption verification.\n" +
                                    "• VIBRATE: Tactile feedback on keypad inputs.\n" +
                                    "• CAMERA / LOCATION / STORAGE: Completely omitted for maximum privacy.",
                            fontFamily = TerminalFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            TerminalFooter()
        }
    }
}
