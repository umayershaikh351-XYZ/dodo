package com.example.domain.model

enum class SecurityScoreBand(val label: String, val level: String) {
    FORTRESS("FORTRESS", "OPTIMAL"),
    SECURE("SECURE", "GOOD"),
    MODERATE_RISK("MODERATE RISK", "WARN"),
    HIGH_RISK("HIGH RISK", "ELEVATED"),
    CRITICAL("CRITICAL", "DANGER")
}

data class IdentityIntel(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val product: String,
    val hardware: String,
    val board: String,
    val bootloader: String,
    val buildId: String,
    val fingerprint: String,
    val buildType: String,
    val tags: String,
    val buildTimeFormatted: String,
    val androidVersion: String,
    val sdkInt: Int,
    val securityPatch: String,
    val kernelVersion: String,
    val vmVersion: String,
    val supportedAbis: String,
    val oneUiVersion: String,
    val knoxVersion: String
)

data class HardwareIntel(
    val ramTotalFormatted: String,
    val ramAvailFormatted: String,
    val ramLowMemFlag: Boolean,
    val storageTotalFormatted: String,
    val storageFreeFormatted: String,
    val screenResolution: String,
    val screenDensityDpi: Int,
    val refreshRateHz: Int,
    val cpuCores: Int,
    val cpuArch: String,
    val sensorCount: Int,
    val sensorNamesPreview: String,
    val cameraCount: Int,
    val flashAvailable: Boolean,
    val batteryLevelPercent: Int,
    val batteryStatus: String,
    val batteryTempCelsius: Float,
    val batteryHealth: String,
    val batteryTechnology: String
)

data class SystemStateIntel(
    val isScreenLockSet: Boolean,
    val isBiometricAvailable: Boolean,
    val isDeviceEncrypted: Boolean,
    val isDeveloperOptionsEnabled: Boolean,
    val isUsbDebuggingEnabled: Boolean,
    val isUnknownSourcesAllowed: Boolean,
    val isAccessibilityServiceActive: Boolean,
    val isAutoTimeEnabled: Boolean,
    val uptimeFormatted: String,
    val bootTimeFormatted: String,
    val systemLocale: String,
    val timezone: String,
    val isEmulator: Boolean
)

data class IntegrityIntel(
    val suBinaryPresent: Boolean,
    val testKeysPresent: Boolean,
    val superuserApkPresent: Boolean,
    val magiskPresent: Boolean,
    val busyboxPresent: Boolean,
    val systemWritable: Boolean,
    val dangerousPropsFound: Boolean,
    val knoxPackagesDetected: Boolean,
    val knoxWarrantyBit: String, // "0x0 (INTACT)" or "0x1 (TRIPPED)" or "[BLOCKED BY KNOX]"
    val knoxSecureFolderPresent: Boolean,
    val verdict: String // "CLEAN", "SUSPICIOUS", "ROOTED"
)

data class NetworkIntel(
    val connectionType: String,
    val ssid: String,
    val encryptionType: String,
    val rssiDbm: Int,
    val linkSpeedMbps: Int,
    val carrierName: String,
    val simCountryIso: String,
    val simOperator: String,
    val isDualSimDetected: Boolean,
    val slot1Details: String,
    val slot2Details: String,
    val isRoaming: Boolean,
    val isOpenOrWepWifi: Boolean
)

data class DeviceIntelProfile(
    val identity: IdentityIntel,
    val hardware: HardwareIntel,
    val systemState: SystemStateIntel,
    val integrity: IntegrityIntel,
    val network: NetworkIntel,
    val timestamp: Long = System.currentTimeMillis()
)

data class SecurityCheckItem(
    val id: String,
    val title: String,
    val deductionPoints: Int,
    val passed: Boolean,
    val description: String,
    val remediation: String,
    val settingsAction: String? = null
)

data class SecurityScoreResult(
    val score: Int,
    val band: SecurityScoreBand,
    val checks: List<SecurityCheckItem>,
    val scanTimestamp: Long = System.currentTimeMillis()
)
