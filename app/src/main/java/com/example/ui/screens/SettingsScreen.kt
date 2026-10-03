package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    autoLockSeconds: Int,
    isSoundEnabled: Boolean,
    isBiometricEnabled: Boolean,
    onAutoLockChanged: (Int) -> Unit,
    onSoundToggled: (Boolean) -> Unit,
    onBiometricToggled: (Boolean) -> Unit,
    onChangePinRequested: () -> Unit,
    onRescanRequested: () -> Unit,
    onClearAllVaultRequested: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showPurgeConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
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
                            text = "> CONFIGURATION TERMINAL",
                            fontFamily = TerminalFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            text = "SECURITY PARAMETERS & AUDIT TIMERS",
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            color = MatrixGreenDim
                        )
                    }
                    TerminalButton(
                        text = "[ BACK ]",
                        onClick = onNavigateBack,
                        type = TerminalButtonType.SECONDARY,
                        testTag = "settings_back_btn"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-lock timer options
                Text(
                    text = "> AUTO-LOCK TIMER INTERVAL:",
                    fontFamily = TerminalFontFamily,
                    fontSize = 12.sp,
                    color = MatrixGreenDim,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                val timerOptions = listOf(15 to "15s", 30 to "30s", 60 to "1m", 300 to "5m")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timerOptions.forEach { (sec, label) ->
                        val isSelected = autoLockSeconds == sec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) MatrixGreen else BackgroundSurface)
                                .border(1.dp, if (isSelected) MatrixGreen else BorderDefault, RoundedCornerShape(3.dp))
                                .clickable { onAutoLockChanged(sec) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = TerminalFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BackgroundPrimary else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggles: Sound & Biometrics
                TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBiometricToggled(!isBiometricEnabled) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "BIOMETRIC AUTHENTICATION",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Allow fingerprint/face scanner unlock",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = if (isBiometricEnabled) "[ ON ]" else "[ OFF ]",
                                fontFamily = TerminalFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBiometricEnabled) StatusOk else TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderDefault)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSoundToggled(!isSoundEnabled) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AUDIO FEEDBACK SYNTH",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Mechanical key clicks & scan beeps",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = if (isSoundEnabled) "[ ON ]" else "[ OFF ]",
                                fontFamily = TerminalFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSoundEnabled) StatusInfo else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                TerminalButton(
                    text = "[ CHANGE MASTER PIN ]",
                    onClick = onChangePinRequested,
                    modifier = Modifier.fillMaxWidth(),
                    type = TerminalButtonType.PRIMARY,
                    testTag = "change_pin_btn"
                )

                Spacer(modifier = Modifier.height(10.dp))

                TerminalButton(
                    text = "[ RE-RUN DEVICE SCAN ]",
                    onClick = {
                        onRescanRequested()
                        Toast.makeText(context, "TELEMETRY SCAN COMPLETE", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    type = TerminalButtonType.SECONDARY,
                    testTag = "rerun_scan_btn"
                )

                Spacer(modifier = Modifier.height(10.dp))

                TerminalButton(
                    text = "[ ABOUT CIPHERLOCK ]",
                    onClick = onNavigateToAbout,
                    modifier = Modifier.fillMaxWidth(),
                    type = TerminalButtonType.SECONDARY,
                    testTag = "about_nav_btn"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Danger zone: Purge all data
                TerminalButton(
                    text = "[ CLEAR ALL VAULT DATA ]",
                    onClick = { showPurgeConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    type = TerminalButtonType.DANGER,
                    testTag = "purge_vault_btn"
                )
            }

            TerminalFooter()
        }

        // Purge confirmation dialog
        if (showPurgeConfirm) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundPrimary.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                TerminalPanel(
                    modifier = Modifier.padding(24.dp),
                    borderColor = StatusFail
                ) {
                    Column {
                        Text(
                            text = "> NUCLEAR PURGE: VAULT DATA",
                            fontFamily = TerminalFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusFail
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Warning: This will destroy all hardware-encrypted credentials stored in this device database. Cannot be recovered.",
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TerminalButton(
                                text = "CANCEL",
                                onClick = { showPurgeConfirm = false },
                                type = TerminalButtonType.SECONDARY
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TerminalButton(
                                text = "PURGE ALL",
                                onClick = {
                                    onClearAllVaultRequested()
                                    showPurgeConfirm = false
                                    Toast.makeText(context, "ALL VAULT ENTRIES PURGED", Toast.LENGTH_SHORT).show()
                                },
                                type = TerminalButtonType.DANGER
                            )
                        }
                    }
                }
            }
        }
    }
}
