package com.example.domain.service

import android.provider.Settings
import com.example.domain.model.DeviceIntelProfile
import com.example.domain.model.SecurityCheckItem
import com.example.domain.model.SecurityScoreBand
import com.example.domain.model.SecurityScoreResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScoreEngine {

    fun evaluate(profile: DeviceIntelProfile): SecurityScoreResult {
        val checks = mutableListOf<SecurityCheckItem>()

        // 1. Screen lock missing (-25)
        val hasLock = profile.systemState.isScreenLockSet
        checks.add(
            SecurityCheckItem(
                id = "SCREEN_LOCK",
                title = "Device Lock Screen Protected",
                deductionPoints = 25,
                passed = hasLock,
                description = if (hasLock) "Hardware lock screen authentication is enforced." else "No lock screen PIN/Pattern/Password configured.",
                remediation = "Enable biometric or secure PIN lock in System Security settings.",
                settingsAction = Settings.ACTION_SECURITY_SETTINGS
            )
        )

        // 2. Device not encrypted (-20)
        val isEncrypted = profile.systemState.isDeviceEncrypted
        checks.add(
            SecurityCheckItem(
                id = "DEVICE_ENCRYPTION",
                title = "FBE Storage Encryption",
                deductionPoints = 20,
                passed = isEncrypted,
                description = if (isEncrypted) "File-based hardware encryption (AES-256) active." else "Internal storage partitions are unencrypted.",
                remediation = "Enable full device storage encryption.",
                settingsAction = Settings.ACTION_SECURITY_SETTINGS
            )
        )

        // 3. Root detected (-30)
        val isRooted = profile.integrity.verdict == "ROOTED"
        checks.add(
            SecurityCheckItem(
                id = "ROOT_STATUS",
                title = "OS Integrity & Superuser Absence",
                deductionPoints = 30,
                passed = !isRooted,
                description = if (!isRooted) "No unauthorized root su binaries or Magisk detected." else "Root binary or su escalation exploit detected on system.",
                remediation = "Re-flash certified Samsung One UI firmware image to remove root."
            )
        )

        // 4. Developer options enabled (-10)
        val devOptions = profile.systemState.isDeveloperOptionsEnabled
        checks.add(
            SecurityCheckItem(
                id = "DEV_OPTIONS",
                title = "Developer Mode Disabled",
                deductionPoints = 10,
                passed = !devOptions,
                description = if (!devOptions) "Developer settings are disabled." else "Developer settings are currently turned ON, widening attack surface.",
                remediation = "Turn off Developer options in system settings.",
                settingsAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            )
        )

        // 5. USB debugging enabled (-10)
        val usbDebug = profile.systemState.isUsbDebuggingEnabled
        checks.add(
            SecurityCheckItem(
                id = "USB_DEBUGGING",
                title = "ADB USB Debugging Inactive",
                deductionPoints = 10,
                passed = !usbDebug,
                description = if (!usbDebug) "ADB bridge is locked against untrusted physical connections." else "ADB is listening; physical USB access can execute shell commands.",
                remediation = "Disable USB debugging immediately when not tethered to trusted workstation.",
                settingsAction = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            )
        )

        // 6. Unknown sources allowed (-15)
        val unknownSources = profile.systemState.isUnknownSourcesAllowed
        checks.add(
            SecurityCheckItem(
                id = "UNKNOWN_SOURCES",
                title = "Sideloading Restrictions",
                deductionPoints = 15,
                passed = !unknownSources,
                description = if (!unknownSources) "Installing unverified APK packages is restricted." else "Third-party application installation without signature verification permitted.",
                remediation = "Disable unknown app installation sources in app permissions.",
                settingsAction = Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
            )
        )

        // 7. Accessibility service enabled (-10)
        val accessibilityActive = profile.systemState.isAccessibilityServiceActive
        checks.add(
            SecurityCheckItem(
                id = "ACCESSIBILITY_AUDIT",
                title = "Accessibility Exploitation Surface",
                deductionPoints = 10,
                passed = !accessibilityActive,
                description = if (!accessibilityActive) "No active background accessibility overlays or screen sniffers." else "Third-party accessibility services enabled (can capture screen/keystrokes).",
                remediation = "Review installed accessibility services and disable unneeded tools.",
                settingsAction = Settings.ACTION_ACCESSIBILITY_SETTINGS
            )
        )

        // 8. Security patch older than 90 days (-15)
        val isPatchRecent = isSecurityPatchWithin90Days(profile.identity.securityPatch)
        checks.add(
            SecurityCheckItem(
                id = "PATCH_CURRENCY",
                title = "Samsung Android Security Patch Currency",
                deductionPoints = 15,
                passed = isPatchRecent,
                description = if (isPatchRecent) "Security patch level (${profile.identity.securityPatch}) is current." else "Security patch (${profile.identity.securityPatch}) is over 90 days old.",
                remediation = "Check for Samsung One UI software updates over Wi-Fi."
            )
        )

        // 9. Wi-Fi OPEN or WEP (-20)
        val isOpenWifi = profile.network.isOpenOrWepWifi
        checks.add(
            SecurityCheckItem(
                id = "WIFI_ENCRYPTION",
                title = "Wireless Channel Encryption",
                deductionPoints = 20,
                passed = !isOpenWifi,
                description = if (!isOpenWifi) "Connected to secure network or offline (no unencrypted WLAN traffic)." else "Connected to insecure OPEN or WEP network! Traffic is eavesdroppable.",
                remediation = "Disconnect from unencrypted open hotspot and switch to WPA2/WPA3 or cellular.",
                settingsAction = Settings.ACTION_WIFI_SETTINGS
            )
        )

        // 10. Emulator (-20)
        val isEmulator = profile.systemState.isEmulator
        checks.add(
            SecurityCheckItem(
                id = "PHYSICAL_HARDWARE",
                title = "Hardware TPM / Bare Metal Authenticity",
                deductionPoints = 20,
                passed = !isEmulator,
                description = if (!isEmulator) "Verified execution on physical Samsung hardware." else "Virtual machine or hypervisor emulator environment detected.",
                remediation = "Deploy CipherLock on genuine Samsung Galaxy A17 hardware."
            )
        )

        // 11. Knox warranty bit tripped (-20)
        val knoxTripped = profile.integrity.knoxWarrantyBit.contains("TRIPPED")
        checks.add(
            SecurityCheckItem(
                id = "KNOX_INTEGRITY",
                title = "Samsung Knox Hardware eFuse Intact",
                deductionPoints = 20,
                passed = !knoxTripped,
                description = if (!knoxTripped) "Knox warranty bit intact (${profile.integrity.knoxWarrantyBit}). Secure boot validated." else "Knox warranty fuse blown (0x1). Knox containers and TrustZone permanently revoked.",
                remediation = "Knox eFuse is a hardware-level permanent fuse and cannot be reset."
            )
        )

        // 12. Battery temp above 45C (-5)
        val isOverheat = profile.hardware.batteryTempCelsius > 45.0f
        checks.add(
            SecurityCheckItem(
                id = "THERMAL_TELEMETRY",
                title = "Battery & Thermal Operational Bounds",
                deductionPoints = 5,
                passed = !isOverheat,
                description = if (!isOverheat) "Battery temperature (${profile.hardware.batteryTempCelsius}°C) nominal." else "Battery thermal alert (${profile.hardware.batteryTempCelsius}°C > 45°C). Possible hardware stress or rogue process.",
                remediation = "Unplug high-draw charger, cool down device, and terminate background tasks."
            )
        )

        // Calculate score
        var score = 100
        for (check in checks) {
            if (!check.passed) {
                score -= check.deductionPoints
            }
        }
        val clampedScore = score.coerceIn(0, 100)

        val band = when {
            clampedScore >= 85 -> SecurityScoreBand.FORTRESS
            clampedScore >= 70 -> SecurityScoreBand.SECURE
            clampedScore >= 50 -> SecurityScoreBand.MODERATE_RISK
            clampedScore >= 30 -> SecurityScoreBand.HIGH_RISK
            else -> SecurityScoreBand.CRITICAL
        }

        return SecurityScoreResult(
            score = clampedScore,
            band = band,
            checks = checks
        )
    }

    private fun isSecurityPatchWithin90Days(patchDateStr: String): Boolean {
        if (patchDateStr.isEmpty() || patchDateStr == "UNKNOWN") return true
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val patchDate = sdf.parse(patchDateStr) ?: return true
            val now = System.currentTimeMillis()
            val diffDays = (now - patchDate.time) / (1000 * 60 * 60 * 24)
            diffDays <= 90
        } catch (_: Exception) {
            true
        }
    }
}
