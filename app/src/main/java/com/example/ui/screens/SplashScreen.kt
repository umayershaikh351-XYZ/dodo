package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MatrixRain
import com.example.ui.components.ScanlineOverlay
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.TerminalFontFamily
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val fullTitle = "CIPHERLOCK"
    var displayedLength by remember { mutableIntStateOf(0) }
    var glitchActive by remember { mutableStateOf(false) }
    val glitchOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Matrix rain starts
        delay(200)
        // Type out CIPHERLOCK (40ms per char)
        for (i in 1..fullTitle.length) {
            displayedLength = i
            delay(40)
        }
        // Glitch flicker
        glitchActive = true
        glitchOffset.animateTo(-3f, tween(60))
        glitchOffset.animateTo(3f, tween(60))
        glitchOffset.animateTo(-1f, tween(60))
        glitchOffset.animateTo(0f, tween(60))
        glitchActive = false

        delay(400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        // Matrix rain background at subtle opacity
        MatrixRain(modifier = Modifier.fillMaxSize())
        ScanlineOverlay()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (glitchActive) {
                    // RGB split glitch
                    Text(
                        text = fullTitle.take(displayedLength),
                        fontFamily = TerminalFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red.copy(alpha = 0.7f),
                        letterSpacing = 0.15.sp,
                        modifier = Modifier.offset { IntOffset(-4, 0) }
                    )
                    Text(
                        text = fullTitle.take(displayedLength),
                        fontFamily = TerminalFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusInfo.copy(alpha = 0.7f),
                        letterSpacing = 0.15.sp,
                        modifier = Modifier.offset { IntOffset(4, 0) }
                    )
                }
                Text(
                    text = fullTitle.take(displayedLength),
                    fontFamily = TerminalFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen,
                    letterSpacing = 0.15.sp,
                    modifier = Modifier.offset { IntOffset(glitchOffset.value.toInt(), 0) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "> SYSTEM INITIALIZING [OFFLINE MODE]",
                fontFamily = TerminalFontFamily,
                fontSize = 11.sp,
                color = MatrixGreenDim,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
