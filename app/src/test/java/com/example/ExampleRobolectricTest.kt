package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.DeviceIntelProfile
import com.example.domain.model.HardwareIntel
import com.example.domain.model.IdentityIntel
import com.example.domain.model.IntegrityIntel
import com.example.domain.model.NetworkIntel
import com.example.domain.model.SecurityScoreBand
import com.example.domain.model.SystemStateIntel
import com.example.domain.service.ScoreEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CipherLock", appName)
    }

    @Test
    fun `test score engine evaluates secure device`() {
        val engine = ScoreEngine()
        val mockProfile = DeviceIntelProfile(
            identity = IdentityIntel(
                manufacturer = "SAMSUNG",
                brand = "SAMSUNG",
                model = "SM-A176B/DS",
                device = "a17x",
                product = "a17xeea",
                hardware = "s5e8845",
                board = "s5e8845",
                bootloader = "A176BXXU1AXA1",
                buildId = "UP1A.231005.007",
                fingerprint = "samsung/a17/a17:16/UP1A.231005.007/A176BXXU1AXA1:user/release-keys",
                buildType = "user",
                tags = "release-keys",
                buildTimeFormatted = "2026-02-14 10:00:00",
                androidVersion = "16",
                sdkInt = 36,
                securityPatch = "2026-09-01",
                kernelVersion = "6.6.0-android16",
                vmVersion = "2.1.0",
                supportedAbis = "arm64-v8a",
                oneUiVersion = "One UI 8.5",
                knoxVersion = "Knox 3.13"
            ),
            hardware = HardwareIntel(
                ramTotalFormatted = "6.0 GB",
                ramAvailFormatted = "3.2 GB",
                ramLowMemFlag = false,
                storageTotalFormatted = "128.0 GB",
                storageFreeFormatted = "84.5 GB",
                screenResolution = "1080x2340",
                screenDensityDpi = 399,
                refreshRateHz = 90,
                cpuCores = 8,
                cpuArch = "aarch64",
                sensorCount = 28,
                sensorNamesPreview = "Accelerometer, Gyroscope, Magnetometer",
                cameraCount = 3,
                flashAvailable = true,
                batteryLevelPercent = 88,
                batteryStatus = "DISCHARGING",
                batteryTempCelsius = 31.4f,
                batteryHealth = "GOOD",
                batteryTechnology = "Li-ion"
            ),
            systemState = SystemStateIntel(
                isScreenLockSet = true,
                isBiometricAvailable = true,
                isDeviceEncrypted = true,
                isDeveloperOptionsEnabled = false,
                isUsbDebuggingEnabled = false,
                isUnknownSourcesAllowed = false,
                isAccessibilityServiceActive = false,
                isAutoTimeEnabled = true,
                uptimeFormatted = "14h 22m",
                bootTimeFormatted = "2026-10-02 12:38",
                systemLocale = "en-US",
                timezone = "America/New_York",
                isEmulator = false
            ),
            integrity = IntegrityIntel(
                suBinaryPresent = false,
                testKeysPresent = false,
                superuserApkPresent = false,
                magiskPresent = false,
                busyboxPresent = false,
                systemWritable = false,
                dangerousPropsFound = false,
                knoxPackagesDetected = true,
                knoxWarrantyBit = "0x0 (INTACT)",
                knoxSecureFolderPresent = true,
                verdict = "CLEAN"
            ),
            network = NetworkIntel(
                connectionType = "WI-FI",
                ssid = "SECURE_OFFICE",
                encryptionType = "WPA3",
                rssiDbm = -54,
                linkSpeedMbps = 866,
                carrierName = "T-Mobile",
                simCountryIso = "US",
                simOperator = "T-Mobile",
                isDualSimDetected = true,
                slot1Details = "SLOT 1: T-Mobile (US)",
                slot2Details = "SLOT 2: Vodafone (GB)",
                isRoaming = false,
                isOpenOrWepWifi = false
            )
        )

        val result = engine.evaluate(mockProfile)
        assertEquals(100, result.score)
        assertEquals(SecurityScoreBand.FORTRESS, result.band)
        assertTrue(result.checks.all { it.passed })
    }
}
