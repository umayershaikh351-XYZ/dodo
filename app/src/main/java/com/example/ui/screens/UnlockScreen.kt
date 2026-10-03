package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScanlineOverlay
import com.example.ui.components.TerminalFooter
import com.example.ui.components.TypewriterText
import com.example.ui.theme.BackgroundElevated
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UnlockScreen(
    lockoutSeconds: Int,
    isBiometricAvailable: Boolean,
    onVerifyPin: (String) -> Boolean,
    onBiometricRequested: () -> Unit,
    onUnlockSuccess: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var showErrorText by remember { mutableStateOf(false) }
    var redFlashActive by remember { mutableStateOf(false) }
    var greenSuccessFlash by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }
    val panelScale = remember { Animatable(1.0f) }

    fun triggerWrongPinAnimation() {
        showErrorText = true
        redFlashActive = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        coroutineScope.launch {
            // Red flash overlay fade
            delay(100)
            redFlashActive = false
        }
        coroutineScope.launch {
            // Shake: 5 cycles of ±8dp
            for (i in 0 until 5) {
                shakeOffset.animateTo(8f, tween(30))
                shakeOffset.animateTo(-8f, tween(30))
            }
            shakeOffset.animateTo(0f, tween(30))
            enteredPin = ""
        }
    }

    fun triggerSuccessAnimation() {
        greenSuccessFlash = true
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        coroutineScope.launch {
            panelScale.animateTo(0.98f, tween(100))
            panelScale.animateTo(1.0f, tween(100))
            delay(100)
            onUnlockSuccess()
        }
    }

    fun onDigit(digit: String) {
        if (lockoutSeconds > 0) return
        showErrorText = false
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (enteredPin.length < 4) {
            val updated = enteredPin + digit
            enteredPin = updated
            if (updated.length == 4) {
                val correct = onVerifyPin(updated)
                if (correct) {
                    triggerSuccessAnimation()
                } else {
                    triggerWrongPinAnimation()
                }
            }
        }
    }

    fun onBackspace() {
        if (lockoutSeconds > 0) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        ScanlineOverlay()

        // Red flash overlay on wrong pin
        if (redFlashActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StatusFail.copy(alpha = 0.30f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .scale(panelScale.value)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "> ENTER ACCESS CODE",
                    fontFamily = TerminalFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen,
                    letterSpacing = 0.08.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "HARDWARE-ENCRYPTED DEVICE TERMINAL",
                    fontFamily = TerminalFontFamily,
                    fontSize = 11.sp,
                    color = MatrixGreenDim,
                    letterSpacing = 0.05.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // PIN Dots container with shake
                Row(
                    modifier = Modifier.offset { IntOffset(shakeOffset.value.toInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        val dotBorder = when {
                            greenSuccessFlash -> MatrixGreen
                            redFlashActive -> StatusFail
                            isFilled -> MatrixGreen
                            else -> BorderDefault
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isFilled) BackgroundElevated else BackgroundSurface)
                                .border(1.dp, dotBorder, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFilled) {
                                Text(
                                    text = "*",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (greenSuccessFlash) StatusInfo else MatrixGreen
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

                Spacer(modifier = Modifier.height(24.dp))

                if (lockoutSeconds > 0) {
                    Text(
                        text = "> SECURITY LOCKOUT: ${lockoutSeconds}s REMAINING",
                        fontFamily = TerminalFontFamily,
                        fontSize = 12.sp,
                        color = StatusFail,
                        fontWeight = FontWeight.Bold
                    )
                } else if (showErrorText) {
                    TypewriterText(
                        fullText = "> ACCESS DENIED. ATTEMPT LOGGED.",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            color = StatusFail,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    Text(
                        text = "> TERMINAL LOCKED",
                        fontFamily = TerminalFontFamily,
                        fontSize = 11.sp,
                        color = MatrixGreenDim
                    )
                }
            }

            // Keypad
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
                    listOf(if (isBiometricAvailable) "BIO" else "", "0", "DEL")
                )

                padRows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(72.dp, 52.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp, 52.dp)
                                        .testTag("unlock_key_$key")
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BackgroundSurface)
                                        .border(
                                            1.dp,
                                            if (key == "BIO") StatusInfo.copy(alpha = 0.6f) else BorderDefault,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable {
                                            when (key) {
                                                "DEL" -> onBackspace()
                                                "BIO" -> onBiometricRequested()
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
                                        color = when (key) {
                                            "BIO" -> StatusInfo
                                            "DEL" -> TextSecondary
                                            else -> TextPrimary
                                        }
                                    )
                                }
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
