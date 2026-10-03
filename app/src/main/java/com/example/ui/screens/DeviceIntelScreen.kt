package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DeviceIntelProfile
import com.example.ui.components.RadarSweep
import com.example.ui.components.ScanlineOverlay
import com.example.ui.components.StatusIndicator
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalButtonType
import com.example.ui.components.TerminalFooter
import com.example.ui.components.TerminalPanel
import com.example.ui.components.TerminalRow
import com.example.ui.components.TerminalStatusBar
import com.example.ui.components.WaveformBars
import com.example.ui.theme.BackgroundPrimary
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
fun DeviceIntelScreen(
    profile: DeviceIntelProfile?,
    onNavigateBack: () -> Unit,
    onRescan: () -> Unit,
    onCopyToClipboard: (String, String) -> Unit
) {
    val context = LocalContext.current

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
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "> DEVICE INTELLIGENCE TERMINAL",
                            fontFamily = TerminalFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            text = "SAMSUNG GALAXY A17 5G (SM-A176B/DS)",
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            color = MatrixGreenDim
                        )
                    }
                    TerminalButton(
                        text = "[ BACK ]",
                        onClick = onNavigateBack,
                        type = TerminalButtonType.SECONDARY,
                        testTag = "intel_back_btn"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (profile == null) {
                    Text(
                        text = "> INITIALIZING HARDWARE SENSORS...",
                        fontFamily = TerminalFontFamily,
                        fontSize = 13.sp,
                        color = StatusInfo
                    )
                } else {
                    // 1. IDENTITY SECTION
                    SectionHeader("01 // HARDWARE & FIRMWARE IDENTITY")
                    TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            TerminalRow("MANUFACTURER", profile.identity.manufacturer)
                            TerminalRow("BRAND", profile.identity.brand)
                            TerminalRow("MODEL", profile.identity.model)
                            TerminalRow("DEVICE", profile.identity.device)
                            TerminalRow("PRODUCT", profile.identity.product)
                            TerminalRow("HARDWARE", profile.identity.hardware)
                            TerminalRow("BOARD", profile.identity.board)
                            TerminalRow("BOOTLOADER", profile.identity.bootloader)
                            TerminalRow("BUILD ID", profile.identity.buildId)
                            TerminalRow("BUILD TYPE", profile.identity.buildType)
                            TerminalRow("TAGS", profile.identity.tags)
                            TerminalRow("ANDROID OS", profile.identity.androidVersion)
                            TerminalRow("API LEVEL", "API ${profile.identity.sdkInt}")
                            TerminalRow("SECURITY PATCH", profile.identity.securityPatch)
                            TerminalRow("ONE UI BUILD", profile.identity.oneUiVersion, valueColor = MatrixGreen)
                            TerminalRow("KNOX ARCH", profile.identity.knoxVersion, valueColor = MatrixGreen)
                            TerminalRow("KERNEL VER", profile.identity.kernelVersion.take(24))
                            TerminalRow("VM ENGINE", profile.identity.vmVersion)
                            TerminalRow("CPU ARCH ABIS", profile.identity.supportedAbis)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. HARDWARE TELEMETRY
                    SectionHeader("02 // HARDWARE & SENSOR PROFILING")
                    TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            TerminalRow("RAM TOTAL", profile.hardware.ramTotalFormatted)
                            TerminalRow("RAM AVAILABLE", profile.hardware.ramAvailFormatted)
                            TerminalRow("STORAGE TOTAL", profile.hardware.storageTotalFormatted)
                            TerminalRow("STORAGE FREE", profile.hardware.storageFreeFormatted)
                            TerminalRow("SCREEN RES", profile.hardware.screenResolution)
                            TerminalRow("DENSITY", "${profile.hardware.screenDensityDpi} DPI")
                            TerminalRow("REFRESH RATE", "${profile.hardware.refreshRateHz} Hz")
                            TerminalRow("CPU CORES", "${profile.hardware.cpuCores} CORES (${profile.hardware.cpuArch})")
                            TerminalRow("ACTIVE SENSORS", "${profile.hardware.sensorCount} DETECTED")
                            TerminalRow("CAMERAS", "${profile.hardware.cameraCount} SENSORS (FLASH: ${if (profile.hardware.flashAvailable) "YES" else "NO"})")
                            TerminalRow("BATTERY LEVEL", "${profile.hardware.batteryLevelPercent}% (${profile.hardware.batteryStatus})")
                            TerminalRow("BATTERY TEMP", "${profile.hardware.batteryTempCelsius}°C", valueColor = if (profile.hardware.batteryTempCelsius > 45f) StatusFail else MatrixGreen)
                            TerminalRow("BATTERY HEALTH", profile.hardware.batteryHealth)
                            TerminalRow("BATTERY TECH", profile.hardware.batteryTechnology)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "> 24-PT BATTERY DISCHARGE TELEMETRY",
                                fontFamily = TerminalFontFamily,
                                fontSize = 10.sp,
                                color = MatrixGreenDim
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            WaveformBars(currentLevelPercent = profile.hardware.batteryLevelPercent)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. SYSTEM STATE & ENFORCEMENT
                    SectionHeader("03 // SYSTEM STATE & SECURITY CONTROLS")
                    TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            TerminalRow("LOCK SCREEN AUTH", if (profile.systemState.isScreenLockSet) "CONFIGURED" else "MISSING") {
                                StatusIndicator(status = if (profile.systemState.isScreenLockSet) "OK" else "FAIL")
                            }
                            TerminalRow("BIOMETRIC SCANNER", if (profile.systemState.isBiometricAvailable) "READY" else "UNAVAILABLE") {
                                StatusIndicator(status = if (profile.systemState.isBiometricAvailable) "OK" else "WARN")
                            }
                            TerminalRow("FBE ENCRYPTION", if (profile.systemState.isDeviceEncrypted) "ACTIVE (AES-256)" else "UNENCRYPTED") {
                                StatusIndicator(status = if (profile.systemState.isDeviceEncrypted) "OK" else "FAIL")
                            }
                            TerminalRow("DEVELOPER OPTIONS", if (profile.systemState.isDeveloperOptionsEnabled) "ENABLED" else "DISABLED") {
                                StatusIndicator(status = if (!profile.systemState.isDeveloperOptionsEnabled) "OK" else "WARN")
                            }
                            TerminalRow("USB ADB DEBUGGING", if (profile.systemState.isUsbDebuggingEnabled) "LISTENING" else "DISABLED") {
                                StatusIndicator(status = if (!profile.systemState.isUsbDebuggingEnabled) "OK" else "WARN")
                            }
                            TerminalRow("UNKNOWN SOURCES", if (profile.systemState.isUnknownSourcesAllowed) "ALLOWED" else "RESTRICTED") {
                                StatusIndicator(status = if (!profile.systemState.isUnknownSourcesAllowed) "OK" else "WARN")
                            }
                            TerminalRow("ACCESSIBILITY OVERLAYS", if (profile.systemState.isAccessibilityServiceActive) "ACTIVE" else "NONE DETECTED") {
                                StatusIndicator(status = if (!profile.systemState.isAccessibilityServiceActive) "OK" else "WARN")
                            }
                            TerminalRow("SYSTEM UPTIME", profile.systemState.uptimeFormatted)
                            TerminalRow("LAST BOOT", profile.systemState.bootTimeFormatted)
                            TerminalRow("LOCALE / TZ", "${profile.systemState.systemLocale} / ${profile.systemState.timezone}")
                            TerminalRow("EMULATOR ARTIFACTS", if (profile.systemState.isEmulator) "DETECTED" else "BARE METAL") {
                                StatusIndicator(status = if (!profile.systemState.isEmulator) "OK" else "FAIL")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. INTEGRITY & KNOX SUITE
                    SectionHeader("04 // INTEGRITY & SAMSUNG KNOX 3.13")
                    TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            TerminalRow("SU BINARY CHECK", if (profile.integrity.suBinaryPresent) "FOUND" else "ABSENT") {
                                StatusIndicator(status = if (!profile.integrity.suBinaryPresent) "OK" else "FAIL")
                            }
                            TerminalRow("BUILD TAGS", if (profile.integrity.testKeysPresent) "TEST-KEYS" else "RELEASE-KEYS") {
                                StatusIndicator(status = if (!profile.integrity.testKeysPresent) "OK" else "WARN")
                            }
                            TerminalRow("MAGISK DETECTOR", if (profile.integrity.magiskPresent) "DETECTED" else "CLEAN") {
                                StatusIndicator(status = if (!profile.integrity.magiskPresent) "OK" else "FAIL")
                            }
                            TerminalRow("BUSYBOX UTILITIES", if (profile.integrity.busyboxPresent) "PRESENT" else "NOT FOUND") {
                                StatusIndicator(status = if (!profile.integrity.busyboxPresent) "OK" else "WARN")
                            }
                            TerminalRow("KNOX CONTAINER SUITE", if (profile.integrity.knoxPackagesDetected) "ACTIVE" else "[BLOCKED BY KNOX]") {
                                StatusIndicator(status = if (profile.integrity.knoxPackagesDetected) "OK" else "WARN")
                            }
                            TerminalRow("KNOX EFUSE WARRANTY", profile.integrity.knoxWarrantyBit) {
                                StatusIndicator(status = if (profile.integrity.knoxWarrantyBit.contains("INTACT")) "OK" else "FAIL")
                            }
                            TerminalRow("SECURE FOLDER STATE", if (profile.integrity.knoxSecureFolderPresent) "INSTALLED" else "UNCONFIGURED")
                            TerminalRow("INTEGRITY VERDICT", profile.integrity.verdict, valueColor = if (profile.integrity.verdict == "CLEAN") StatusOk else StatusFail) {
                                StatusIndicator(status = profile.integrity.verdict)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. NETWORK & DUAL-SIM INTELLIGENCE
                    SectionHeader("05 // NETWORK & DUAL-SIM TELEMETRY")
                    TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    TerminalRow("CONNECTION", profile.network.connectionType)
                                    TerminalRow("WLAN SSID", profile.network.ssid)
                                    TerminalRow("ENCRYPTION", profile.network.encryptionType, valueColor = if (profile.network.isOpenOrWepWifi) StatusFail else MatrixGreen) {
                                        StatusIndicator(status = if (!profile.network.isOpenOrWepWifi) "OK" else "FAIL")
                                    }
                                    TerminalRow("SIGNAL RSSI", "${profile.network.rssiDbm} dBm")
                                    TerminalRow("LINK SPEED", "${profile.network.linkSpeedMbps} Mbps")
                                    TerminalRow("CARRIER", profile.network.carrierName)
                                    TerminalRow("DUAL SIM HARDWARE", if (profile.network.isDualSimDetected) "ACTIVE (DUAL STANDBY)" else "SINGLE SIM")
                                    TerminalRow("SIM 1", profile.network.slot1Details)
                                    TerminalRow("SIM 2", profile.network.slot2Details)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                RadarSweep()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. RESTRICTED SENSITIVE HARDWARE ATTRIBUTES
                    SectionHeader("06 // RESTRICTED DEVICE SENSORS (POLICY ENFORCED)")
                    TerminalPanel(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            TerminalRow("IMEI 1 / MEID", "[RESTRICTED BY ANDROID]")
                            TerminalRow("IMEI 2 (DUAL-SIM)", "[RESTRICTED BY ANDROID]")
                            TerminalRow("IMSI / ICCID", "[RESTRICTED BY ANDROID]")
                            TerminalRow("DEVICE SERIAL NO", "[BLOCKED BY KNOX]")
                            TerminalRow("PHYSICAL GPS LOC", "[PERMISSION OMITTED - OFFLINE]")
                            TerminalRow("MICROPHONE / CAM FEED", "[NO ACCESS REQUESTED]")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons: Export Profile & Rescan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TerminalButton(
                            text = "[ EXPORT PROFILE ]",
                            onClick = {
                                val exportText = buildString {
                                    appendLine("=== CIPHERLOCK DEVICE PROFILE ===")
                                    appendLine("Device: ${profile.identity.manufacturer} ${profile.identity.model}")
                                    appendLine("OS: Android ${profile.identity.androidVersion} | One UI: ${profile.identity.oneUiVersion} | Knox: ${profile.identity.knoxVersion}")
                                    appendLine("RAM: ${profile.hardware.ramTotalFormatted} | Storage: ${profile.hardware.storageTotalFormatted}")
                                    appendLine("Integrity: ${profile.integrity.verdict} | Knox eFuse: ${profile.integrity.knoxWarrantyBit}")
                                    appendLine("Network: ${profile.network.connectionType} (${profile.network.carrierName})")
                                    appendLine("Generated: 100% Offline by CipherLock")
                                }
                                onCopyToClipboard("CipherLock_Profile", exportText)
                                Toast.makeText(context, "DEVICE PROFILE COPIED (AUTOCLEARS IN 30s)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            type = TerminalButtonType.PRIMARY,
                            testTag = "export_profile_btn"
                        )

                        TerminalButton(
                            text = "[ RESCAN ]",
                            onClick = onRescan,
                            modifier = Modifier.weight(1f),
                            type = TerminalButtonType.SECONDARY,
                            testTag = "intel_rescan_btn"
                        )
                    }
                }
            }

            TerminalFooter()
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = "> $title",
        fontFamily = TerminalFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MatrixGreen,
        letterSpacing = 0.08.sp,
        modifier = Modifier.padding(bottom = 6.dp, top = 4.dp)
    )
}
