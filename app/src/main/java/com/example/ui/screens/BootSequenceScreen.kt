package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScanlineOverlay
import com.example.ui.components.StatusIndicator
import com.example.ui.components.TerminalFooter
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.TerminalFontFamily
import kotlinx.coroutines.delay

@Composable
fun BootSequenceScreen(
    onBootFinished: () -> Unit
) {
    val bootLines = listOf(
        "> LOADING KERNEL MODULES...",
        "> MOUNTING ENCRYPTED VOLUME...",
        "> VERIFYING INTEGRITY...",
        "> INITIALIZING DEVICE SCAN...",
        "> SYSTEM READY."
    )

    var currentLineIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        for (i in bootLines.indices) {
            currentLineIndex = i
            delay(160)
        }
        delay(350)
        onBootFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .clickable { onBootFinished() } // Allow skip on tap
    ) {
        ScanlineOverlay()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "> BOOTLOADER PROTOCOL v1.0",
                    fontFamily = TerminalFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatrixGreen,
                    letterSpacing = 0.08.sp
                )
                Text(
                    text = "TARGET: SAMSUNG SM-A176B/DS | ONE UI 8.5",
                    fontFamily = TerminalFontFamily,
                    fontSize = 11.sp,
                    color = MatrixGreenDim,
                    letterSpacing = 0.05.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                for (idx in 0..currentLineIndex) {
                    val line = bootLines[idx]
                    val isDone = idx < currentLineIndex || idx == bootLines.lastIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = line,
                            fontFamily = TerminalFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = MatrixGreen
                        )
                        if (idx < bootLines.size - 1) {
                            StatusIndicator(status = if (isDone) "OK" else "..")
                        } else {
                            Text(
                                text = "[ONLINE]",
                                fontFamily = TerminalFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreen
                            )
                        }
                    }
                }
            }

            Column {
                Text(
                    text = "[ TAP TO SKIP BOOT ANIMATION ]",
                    fontFamily = TerminalFontFamily,
                    fontSize = 11.sp,
                    color = MatrixGreenDim,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TerminalFooter()
            }
        }
    }
}
