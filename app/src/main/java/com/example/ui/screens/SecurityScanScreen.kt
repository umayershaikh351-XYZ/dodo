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
import com.example.domain.model.SecurityScoreResult
import com.example.ui.components.ScanlineOverlay
import com.example.ui.components.StatusIndicator
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalButtonType
import com.example.ui.components.TerminalFooter
import com.example.ui.components.TerminalPanel
import com.example.ui.components.TerminalStatusBar
import com.example.ui.theme.BackgroundElevated
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SecurityScanScreen(
    scoreResult: SecurityScoreResult?,
    onNavigateBack: () -> Unit,
    onRescan: () -> Unit
) {
    val context = LocalContext.current
    val score = scoreResult?.score ?: 100
    val checks = scoreResult?.checks ?: emptyList()

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
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "> 12-POINT SECURITY AUDIT",
                            fontFamily = TerminalFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            text = "SCORE: $score / 100 [${scoreResult?.band?.label ?: "SECURE"}]",
                            fontFamily = TerminalFontFamily,
                            fontSize = 11.sp,
                            color = if (score >= 70) StatusOk else if (score >= 50) StatusWarn else StatusFail,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TerminalButton(
                        text = "[ BACK ]",
                        onClick = onNavigateBack,
                        type = TerminalButtonType.SECONDARY,
                        testTag = "scan_back_btn"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Panel
                TerminalPanel(
                    hasAngledCut = true,
                    hasLeftAccentBar = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "VULNERABILITY SURFACE BREAKDOWN",
                            fontFamily = TerminalFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Every deduction reflects an unhardened vector that could allow arbitrary execution, root escalation, or physical memory acquisition.",
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 12 Checks List
                checks.forEachIndexed { index, item ->
                    val checkIndex = String.format("%02d", index + 1)
                    val borderColor = if (item.passed) BorderDefault else StatusWarn.copy(alpha = 0.6f)

                    TerminalPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        borderColor = borderColor
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "[$checkIndex]",
                                        fontFamily = TerminalFontFamily,
                                        fontSize = 11.sp,
                                        color = MatrixGreenDim,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text(
                                        text = item.title,
                                        fontFamily = TerminalFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.passed) TextPrimary else StatusWarn
                                    )
                                }
                                StatusIndicator(status = if (item.passed) "OK" else "WARN")
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = item.description,
                                fontFamily = TerminalFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )

                            if (!item.passed) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "IMPACT: -${item.deductionPoints} PTS",
                                        fontFamily = TerminalFontFamily,
                                        fontSize = 10.sp,
                                        color = StatusFail,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (item.settingsAction != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(BackgroundElevated)
                                                .border(1.dp, MatrixGreen.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                                .clickable {
                                                    try {
                                                        context.startActivity(Intent(item.settingsAction))
                                                    } catch (_: Exception) {
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "[ FIX ]",
                                                fontFamily = TerminalFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MatrixGreen
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "> REMEDIATION: ${item.remediation}",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 10.sp,
                                    color = MatrixGreenDim,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TerminalButton(
                    text = "[ RE-EVALUATE 12-POINT AUDIT ]",
                    onClick = onRescan,
                    modifier = Modifier.fillMaxWidth(),
                    type = TerminalButtonType.PRIMARY,
                    testTag = "audit_rescan_btn"
                )
            }

            TerminalFooter()
        }
    }
}
