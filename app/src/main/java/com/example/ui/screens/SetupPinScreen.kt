package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScanlineOverlay
import com.example.ui.components.TerminalFooter
import com.example.ui.components.TerminalPanel
import com.example.ui.theme.BackgroundElevated
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusFail
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SetupPinScreen(
    onPinSetupComplete: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentInput = if (isConfirmStep) confirmPin else firstPin

    fun onDigit(digit: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        errorMessage = null
        if (currentInput.length < 4) {
            val updated = currentInput + digit
            if (!isConfirmStep) {
                firstPin = updated
                if (updated.length == 4) {
                    isConfirmStep = true
                }
            } else {
                confirmPin = updated
                if (updated.length == 4) {
                    if (updated == firstPin) {
                        onPinSetupComplete(updated)
                    } else {
                        errorMessage = "> PINS DO NOT MATCH. RETRY."
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        firstPin = ""
                        confirmPin = ""
                        isConfirmStep = false
                    }
                }
            }
        }
    }

    fun onBackspace() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        errorMessage = null
        if (!isConfirmStep) {
            if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
        } else {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                isConfirmStep = false
                firstPin = ""
            }
        }
    }

    fun onClear() {
        firstPin = ""
        confirmPin = ""
        isConfirmStep = false
        errorMessage = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        ScanlineOverlay()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "> MASTER PIN CREATION",
                    fontFamily = TerminalFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen,
                    letterSpacing = 0.08.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (!isConfirmStep) "STEP 1: ENTER 4-DIGIT PIN" else "STEP 2: CONFIRM 4-DIGIT PIN",
                    fontFamily = TerminalFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    letterSpacing = 0.05.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < currentInput.length
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isFilled) BackgroundElevated else BackgroundSurface)
                                .border(
                                    width = 1.dp,
                                    color = if (isFilled) MatrixGreen else BorderDefault,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFilled) {
                                Text(
                                    text = "*",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixGreen
                                )
                            } else {
                                Text(
                                    text = "_",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 16.sp,
                                    color = MatrixGreenDim
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontFamily = TerminalFontFamily,
                        fontSize = 12.sp,
                        color = StatusFail,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "> HARDWARE KEYSTORE SEED READY",
                        fontFamily = TerminalFontFamily,
                        fontSize = 11.sp,
                        color = MatrixGreenDim
                    )
                }
            }

            // Monospace Hacker Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val padRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("CLR", "0", "DEL")
                )

                padRows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp, 52.dp)
                                    .testTag("pin_key_$key")
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BackgroundSurface)
                                    .border(1.dp, BorderDefault, RoundedCornerShape(4.dp))
                                    .clickable {
                                        when (key) {
                                            "DEL" -> onBackspace()
                                            "CLR" -> onClear()
                                            else -> onDigit(key)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (key == "DEL" || key == "CLR") TextSecondary else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TerminalFooter()
            }
        }
    }
}
