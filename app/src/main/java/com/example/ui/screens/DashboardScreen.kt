package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DeviceIntelProfile
import com.example.domain.model.SecurityScoreResult
import com.example.ui.components.ScanlineOverlay
import com.example.ui.components.ScoreArc
import com.example.ui.components.StatusIndicator
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalButtonType
import com.example.ui.components.TerminalFooter
import com.example.ui.components.TerminalPanel
import com.example.ui.components.TerminalRow
import com.example.ui.components.TerminalStatusBar
import com.example.ui.theme.BackgroundElevated
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
fun DashboardScreen(
    scoreResult: SecurityScoreResult?,
    deviceProfile: DeviceIntelProfile?,
    vaultItemCount: Int,
    isScanning: Boolean,
    onNavigateToIntel: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRescanRequested: () -> Unit,
    onLockRequested: () -> Unit
) {
    val context = LocalContext.current
    val score = scoreResult?.score ?: 100
    val bandLabel = scoreResult?.band?.label ?: "SECURE"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        ScanlineOverlay()

        Column(modifier = Modifier.fillMaxSize()) {
            TerminalStatusBar(score = score, isLocked = false)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // Header & Quick Lock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "> CIPHERLOCK DASHBOARD",
                            fontFamily = TerminalFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            text = "SECURE CORE // ZERO NETWORK LEAK",
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            color = MatrixGreenDim
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BackgroundSurface)
                            .border(1.dp, BorderDefault, RoundedCornerShape(4.dp))
                            .clickable { onLockRequested() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "[ LOCK ]",
                            fontFamily = TerminalFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusWarn
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Score Panel with Cyberpunk Angled Corner
                TerminalPanel(
                    hasAngledCut = true,
                    hasLeftAccentBar = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScoreArc(score = score, bandLabel = bandLabel)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "INTEGRITY",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = deviceProfile?.integrity?.verdict ?: "CLEAN",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (deviceProfile?.integrity?.verdict == "CLEAN") StatusOk else StatusFail
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "ENCRYPTED VAULT",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "$vaultItemCount ITEMS",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusInfo
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "KNOX STATUS",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = if (deviceProfile?.integrity?.knoxWarrantyBit?.contains("INTACT") == true) "INTACT" else "SECURED",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scanning Progress bar if active
                if (isScanning) {
                    TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "> SCANNING DEVICE HARMONICS...",
                                fontFamily = TerminalFontFamily,
                                fontSize = 12.sp,
                                color = StatusInfo,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(BackgroundElevated)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.75f)
                                        .height(4.dp)
                                        .background(MatrixGreen)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Quick Navigation Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TerminalButton(
                        text = "[ VAULT ]",
                        onClick = onNavigateToVault,
                        modifier = Modifier.weight(1f),
                        type = TerminalButtonType.PRIMARY,
                        testTag = "dash_vault_btn"
                    )
                    TerminalButton(
                        text = "[ AUDIT (12) ]",
                        onClick = onNavigateToScan,
                        modifier = Modifier.weight(1f),
                        type = TerminalButtonType.PRIMARY,
                        testTag = "dash_audit_btn"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TerminalButton(
                        text = "[ DEVICE INTEL ]",
                        onClick = onNavigateToIntel,
                        modifier = Modifier.weight(1f),
                        type = TerminalButtonType.SECONDARY,
                        testTag = "dash_intel_btn"
                    )
                    TerminalButton(
                        text = "[ SETTINGS ]",
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f),
                        type = TerminalButtonType.SECONDARY,
                        testTag = "dash_settings_btn"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security Deductions Section
                Text(
                    text = "> CRITICAL DEFENSIVE FINDINGS",
                    fontFamily = TerminalFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen
                )
                Spacer(modifier = Modifier.height(8.dp))

                val failedChecks = scoreResult?.checks?.filter { !it.passed } ?: emptyList()
                if (failedChecks.isEmpty()) {
                    TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "NO ACTIVE VULNERABILITIES DETECTED",
                                fontFamily = TerminalFontFamily,
                                fontSize = 12.sp,
                                color = StatusOk,
                                fontWeight = FontWeight.Bold
                            )
                            StatusIndicator(status = "OK")
                        }
                    }
                } else {
                    failedChecks.take(3).forEach { check ->
                        TerminalPanel(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            borderColor = StatusWarn.copy(alpha = 0.5f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = check.title,
                                        fontFamily = TerminalFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusWarn
                                    )
                                    Text(
                                        text = "-${check.deductionPoints} PTS",
                                        fontFamily = TerminalFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusFail
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = check.description,
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (check.settingsAction != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(BackgroundElevated)
                                            .border(1.dp, BorderDefault, RoundedCornerShape(2.dp))
                                            .clickable {
                                                try {
                                                    context.startActivity(Intent(check.settingsAction))
                                                } catch (_: Exception) {
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "[ FIX IN SYSTEM SETTINGS ]",
                                            fontFamily = TerminalFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MatrixGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rescan trigger
                TerminalButton(
                    text = "[ RESCAN DEVICE TELEMETRY ]",
                    onClick = onRescanRequested,
                    modifier = Modifier.fillMaxWidth(),
                    type = TerminalButtonType.SECONDARY,
                    testTag = "rescan_button"
                )
            }

            TerminalFooter()
        }
    }
}
