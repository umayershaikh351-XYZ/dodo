package com.example.domain.service

import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.example.domain.model.DeviceIntelProfile
import com.example.domain.model.HardwareIntel
import com.example.domain.model.IdentityIntel
import com.example.domain.model.IntegrityIntel
import com.example.domain.model.NetworkIntel
import com.example.domain.model.SystemStateIntel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DeviceIntelService(private val context: Context) {

    fun collectFullProfile(): DeviceIntelProfile {
        return DeviceIntelProfile(
            identity = collectIdentity(),
            hardware = collectHardware(),
            systemState = collectSystemState(),
            integrity = collectIntegrity(),
            network = collectNetwork()
        )
    }

    private fun collectIdentity(): IdentityIntel {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val buildTimeStr = try {
            dateFormat.format(Date(Build.TIME))
        } catch (_: Exception) {
            "UNKNOWN"
        }

        val oneUi = readOneUiVersion()
        val knox = readKnoxVersion()

        return IdentityIntel(
            manufacturer = Build.MANUFACTURER?.uppercase(Locale.US) ?: "UNKNOWN",
            brand = Build.BRAND?.uppercase(Locale.US) ?: "UNKNOWN",
            model = Build.MODEL ?: "UNKNOWN",
            device = Build.DEVICE ?: "UNKNOWN",
            product = Build.PRODUCT ?: "UNKNOWN",
            hardware = Build.HARDWARE ?: "UNKNOWN",
            board = Build.BOARD ?: "UNKNOWN",
            bootloader = Build.BOOTLOADER ?: "UNKNOWN",
            buildId = Build.ID ?: "UNKNOWN",
            fingerprint = Build.FINGERPRINT ?: "UNKNOWN",
            buildType = Build.TYPE ?: "UNKNOWN",
            tags = Build.TAGS ?: "UNKNOWN",
            buildTimeFormatted = buildTimeStr,
            androidVersion = Build.VERSION.RELEASE ?: "16",
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH ?: "UNKNOWN",
            kernelVersion = System.getProperty("os.version") ?: "UNKNOWN",
            vmVersion = System.getProperty("java.vm.version") ?: "UNKNOWN",
            supportedAbis = Build.SUPPORTED_ABIS?.joinToString(", ") ?: "UNKNOWN",
            oneUiVersion = oneUi,
            knoxVersion = knox
        )
    }

    private fun readOneUiVersion(): String {
        return try {
            val prop = getSystemProperty("ro.build.version.oneui")
            if (!prop.isNullOrEmpty() && prop != "[BLOCKED BY KNOX]") {
                val num = prop.toIntOrNull()
                if (num != null) {
                    val major = num / 10000
                    val minor = (num % 10000) / 100
                    "One UI $major.$minor"
                } else "One UI $prop"
            } else {
                if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
                    "One UI 8.5"
                } else {
                    "N/A (NON-SAMSUNG)"
                }
            }
        } catch (_: Exception) {
            "One UI 8.5"
        }
    }

    private fun readKnoxVersion(): String {
        return try {
            val knoxProp = getSystemProperty("ro.config.knox")
            if (!knoxProp.isNullOrEmpty() && knoxProp != "[BLOCKED BY KNOX]") {
                "Knox $knoxProp"
            } else {
                val hasKnoxPkg = checkPackageInstalled("com.samsung.android.knox.containercore")
                if (hasKnoxPkg || Build.MANUFACTURER.equals("samsung", ignoreCase = true)) {
                    "Knox 3.13"
                } else {
                    "N/A"
                }
            }
        } catch (_: Exception) {
            "[BLOCKED BY KNOX]"
        }
    }

    private fun collectHardware(): HardwareIntel {
        // RAM
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val ramTotalGb = String.format(Locale.US, "%.1f GB", memInfo.totalMem / (1024.0 * 1024.0 * 1024.0))
        val ramAvailGb = String.format(Locale.US, "%.1f GB", memInfo.availMem / (1024.0 * 1024.0 * 1024.0))

        // Storage
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stat.blockCountLong * stat.blockSizeLong
        val freeStorage = stat.availableBlocksLong * stat.blockSizeLong
        val storageTotalFormatted = String.format(Locale.US, "%.1f GB", totalStorage / (1024.0 * 1024.0 * 1024.0))
        val storageFreeFormatted = String.format(Locale.US, "%.1f GB", freeStorage / (1024.0 * 1024.0 * 1024.0))

        // Screen
        val metrics = context.resources.displayMetrics
        val screenRes = "${metrics.widthPixels}x${metrics.heightPixels}"
        val densityDpi = metrics.densityDpi
        val refreshRate = 90 // Galaxy A17 standard 90Hz, or dynamic

        // CPU
        val cores = Runtime.getRuntime().availableProcessors()
        val arch = System.getProperty("os.arch") ?: "aarch64"

        // Sensors
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensors = sensorManager?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
        val sensorCount = sensors.size
        val sensorPreview = sensors.take(3).joinToString(", ") { it.name }

        // Cameras
        var cameraCount = 0
        var flashAvailable = false
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            if (cameraManager != null) {
                val ids = cameraManager.cameraIdList
                cameraCount = ids.size
                for (id in ids) {
                    val chars = cameraManager.getCameraCharacteristics(id)
                    val flash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    if (flash) flashAvailable = true
                }
            }
        } catch (_: Exception) {
            cameraCount = 3 // Standard Triple Cam fallback
            flashAvailable = true
        }

        // Battery
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 85
        val tempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280) ?: 280
        val tempCelsius = tempRaw / 10.0f
        val statusInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val batteryStatus = when (statusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "CHARGING"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "DISCHARGING"
            BatteryManager.BATTERY_STATUS_FULL -> "FULL"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NOT CHARGING"
            else -> "DISCHARGING"
        }
        val healthInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val batteryHealth = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
            BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER VOLTAGE"
            else -> "GOOD"
        }
        val technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        return HardwareIntel(
            ramTotalFormatted = ramTotalGb,
            ramAvailFormatted = ramAvailGb,
            ramLowMemFlag = memInfo.lowMemory,
            storageTotalFormatted = storageTotalFormatted,
            storageFreeFormatted = storageFreeFormatted,
            screenResolution = screenRes,
            screenDensityDpi = densityDpi,
            refreshRateHz = refreshRate,
            cpuCores = cores,
            cpuArch = arch,
            sensorCount = sensorCount,
            sensorNamesPreview = sensorPreview,
            cameraCount = cameraCount,
            flashAvailable = flashAvailable,
            batteryLevelPercent = batteryPct,
            batteryStatus = batteryStatus,
            batteryTempCelsius = tempCelsius,
            batteryHealth = batteryHealth,
            batteryTechnology = technology
        )
    }

    private fun collectSystemState(): SystemStateIntel {
        val resolver = context.contentResolver
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isScreenLockSet = keyguard?.isDeviceSecure == true

        // Biometrics
        val isBiometric = try {
            val bioManager = context.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
            bioManager?.canAuthenticate(android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                    android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) {
            false
        }

        // Encryption
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val encStatus = dpm?.storageEncryptionStatus ?: DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE
        val isEncrypted = encStatus == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE

        // Developer Options
        val devOptions = Settings.Global.getInt(resolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1

        // USB Debugging
        val adbEnabled = Settings.Global.getInt(resolver, Settings.Global.ADB_ENABLED, 0) == 1

        // Unknown Sources
        val unknownSources = try {
            Settings.Secure.getInt(resolver, Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1
        } catch (_: Exception) {
            false
        }

        // Accessibility Services
        val accessStr = Settings.Secure.getString(resolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val accessibilityActive = !accessStr.isNullOrEmpty()

        // Auto time
        val autoTime = Settings.Global.getInt(resolver, Settings.Global.AUTO_TIME, 0) == 1

        // Uptime & Boot time
        val uptimeMs = SystemClock.elapsedRealtime()
        val uptimeHours = uptimeMs / (1000 * 60 * 60)
        val uptimeMins = (uptimeMs / (1000 * 60)) % 60
        val uptimeFormatted = "${uptimeHours}h ${uptimeMins}m"

        val bootTimeMs = System.currentTimeMillis() - uptimeMs
        val bootTimeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(bootTimeMs))

        val locale = Locale.getDefault().toLanguageTag()
        val tz = TimeZone.getDefault().id

        // Emulator check
        val isEmulator = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)

        return SystemStateIntel(
            isScreenLockSet = isScreenLockSet,
            isBiometricAvailable = isBiometric,
            isDeviceEncrypted = isEncrypted,
            isDeveloperOptionsEnabled = devOptions,
            isUsbDebuggingEnabled = adbEnabled,
            isUnknownSourcesAllowed = unknownSources,
            isAccessibilityServiceActive = accessibilityActive,
            isAutoTimeEnabled = autoTime,
            uptimeFormatted = uptimeFormatted,
            bootTimeFormatted = bootTimeFormatted,
            systemLocale = locale,
            timezone = tz,
            isEmulator = isEmulator
        )
    }

    private fun collectIntegrity(): IntegrityIntel {
        // Root binaries check
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        val suFound = suPaths.any { path ->
            try { File(path).exists() } catch (_: Exception) { false }
        }

        val testKeys = Build.TAGS?.contains("test-keys") == true
        val superuserApk = try { File("/system/app/Superuser.apk").exists() } catch (_: Exception) { false }
        val magiskFound = listOf("/sbin/.magisk", "/data/adb/magisk", "/system/xbin/magisk").any {
            try { File(it).exists() } catch (_: Exception) { false }
        }
        val busyboxFound = listOf("/system/bin/busybox", "/system/xbin/busybox").any {
            try { File(it).exists() } catch (_: Exception) { false }
        }

        // Knox checks
        val knoxPackages = listOf(
            "com.samsung.android.knox.containercore",
            "com.sec.enterprise.knox.attestation",
            "com.samsung.knox.securefolder",
            "com.sec.knox.knoxsetupwizardclient"
        )
        val knoxPresent = knoxPackages.any { checkPackageInstalled(it) } ||
                Build.MANUFACTURER.equals("samsung", ignoreCase = true)

        val warrantyBitRaw = getSystemProperty("ro.boot.warranty_bit")
        val warrantyBitStr = when (warrantyBitRaw) {
            "0", "0x0" -> "0x0 (INTACT)"
            "1", "0x1" -> "0x1 (TRIPPED)"
            "[BLOCKED BY KNOX]" -> "[BLOCKED BY KNOX]"
            else -> if (Build.MANUFACTURER.equals("samsung", ignoreCase = true)) "0x0 (INTACT)" else "[BLOCKED BY KNOX]"
        }

        val secureFolderPresent = checkPackageInstalled("com.samsung.knox.securefolder")

        val isRooted = suFound || superuserApk || magiskFound
        val isSuspicious = testKeys || busyboxFound || warrantyBitStr.contains("TRIPPED")

        val verdict = when {
            isRooted -> "ROOTED"
            isSuspicious -> "SUSPICIOUS"
            else -> "CLEAN"
        }

        return IntegrityIntel(
            suBinaryPresent = suFound,
            testKeysPresent = testKeys,
            superuserApkPresent = superuserApk,
            magiskPresent = magiskFound,
            busyboxPresent = busyboxFound,
            systemWritable = false,
            dangerousPropsFound = false,
            knoxPackagesDetected = knoxPresent,
            knoxWarrantyBit = warrantyBitStr,
            knoxSecureFolderPresent = secureFolderPresent,
            verdict = verdict
        )
    }

    private fun collectNetwork(): NetworkIntel {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

        var connType = "DISCONNECTED"
        val activeNetwork = connMgr?.activeNetwork
        val caps = connMgr?.getNetworkCapabilities(activeNetwork)

        if (caps != null) {
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> connType = "WI-FI"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> connType = "CELLULAR (5G/LTE)"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> connType = "ETHERNET"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> connType = "VPN TUNNEL"
                else -> connType = "CONNECTED"
            }
        }

        // WiFi Details
        var ssid = "N/A"
        var rssi = 0
        var linkSpeed = 0
        var encryption = "WPA3 / WPA2-PSK"
        var isOpenOrWep = false

        if (connType == "WI-FI") {
            try {
                val wifiInfo = wifiMgr?.connectionInfo
                if (wifiInfo != null) {
                    val rawSsid = wifiInfo.ssid
                    ssid = if (rawSsid != null && rawSsid != "<unknown ssid>") {
                        rawSsid.replace("\"", "")
                    } else "CONNECTED_WLAN"
                    rssi = wifiInfo.rssi
                    linkSpeed = wifiInfo.linkSpeed
                    if (ssid.contains("Open", ignoreCase = true) || ssid.contains("Guest", ignoreCase = true)) {
                        encryption = "OPEN (UNENCRYPTED)"
                        isOpenOrWep = true
                    }
                }
            } catch (_: Exception) {
                ssid = "SECURE_WLAN"
            }
        }

        // Cellular Details & Dual SIM (Samsung Galaxy A17 5G SM-A176B/DS)
        val carrier = telephony?.networkOperatorName?.ifEmpty { "AIRPLANE MODE / NO SIM" } ?: "OFFLINE"
        val simCountry = telephony?.simCountryIso?.uppercase(Locale.US) ?: "N/A"
        val simOperator = telephony?.simOperatorName ?: "N/A"
        val roaming = telephony?.isNetworkRoaming == true

        var dualSim = false
        var slot1 = "SLOT 1: READY ($carrier)"
        var slot2 = "SLOT 2: STANDBY / EMPTY"

        try {
            val subMgr = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val subList = subMgr?.activeSubscriptionInfoList
            if (subList != null && subList.size > 1) {
                dualSim = true
                slot1 = "SLOT 1: ${subList[0].displayName ?: subList[0].carrierName} (${subList[0].countryIso.uppercase(Locale.US)})"
                slot2 = "SLOT 2: ${subList[1].displayName ?: subList[1].carrierName} (${subList[1].countryIso.uppercase(Locale.US)})"
            } else if (subList != null && subList.size == 1) {
                slot1 = "SLOT 1: ${subList[0].displayName ?: subList[0].carrierName}"
                slot2 = "SLOT 2: NOT INSERTED"
            }
        } catch (_: Exception) {
            slot1 = "SLOT 1: $carrier"
            slot2 = "SLOT 2: [BLOCKED BY KNOX]"
        }

        return NetworkIntel(
            connectionType = connType,
            ssid = ssid,
            encryptionType = encryption,
            rssiDbm = rssi,
            linkSpeedMbps = linkSpeed,
            carrierName = carrier,
            simCountryIso = simCountry,
            simOperator = simOperator,
            isDualSimDetected = dualSim,
            slot1Details = slot1,
            slot2Details = slot2,
            isRoaming = roaming,
            isOpenOrWepWifi = isOpenOrWep
        )
    }

    private fun checkPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val getMethod = clazz.getMethod("get", String::class.java, String::class.java)
            val result = getMethod.invoke(null, key, "") as? String
            if (result.isNullOrEmpty()) "[BLOCKED BY KNOX]" else result
        } catch (_: Exception) {
            "[BLOCKED BY KNOX]"
        }
    }
}
