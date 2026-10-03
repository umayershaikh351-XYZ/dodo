package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom

class SecurityPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cipherlock_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTOLOCK_SECONDS = "autolock_seconds"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "lockout_until"
        private const val KEY_LAST_SCAN_TIME = "last_scan_time"
    }

    fun hasPin(): Boolean {
        return prefs.contains(KEY_PIN_HASH) && prefs.getString(KEY_PIN_HASH, null)?.isNotEmpty() == true
    }

    fun setPin(pin: String) {
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val salt = saltBytes.joinToString("") { "%02x".format(it) }
        val hash = hashPinWithSalt(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val computedHash = hashPinWithSalt(pin, salt)
        return storedHash == computedHash
    }

    private fun hashPinWithSalt(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$pin::$salt::CipherLock#2026".toByteArray(Charsets.UTF_8)
        val digest = md.digest(combined)
        return digest.joinToString("") { "%02x".format(it) }
    }

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var autoLockSeconds: Int
        get() = prefs.getInt(KEY_AUTOLOCK_SECONDS, 30)
        set(value) = prefs.edit().putInt(KEY_AUTOLOCK_SECONDS, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var isOnboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    var failedAttempts: Int
        get() = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        set(value) = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, value).apply()

    var lockoutUntilTimestamp: Long
        get() = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        set(value) = prefs.edit().putLong(KEY_LOCKOUT_UNTIL, value).apply()

    var lastScanTime: Long
        get() = prefs.getLong(KEY_LAST_SCAN_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SCAN_TIME, value).apply()

    fun recordFailedAttempt(): Int {
        val current = failedAttempts + 1
        failedAttempts = current
        if (current >= 3) {
            lockoutUntilTimestamp = System.currentTimeMillis() + 30_000L
        }
        return current
    }

    fun resetFailedAttempts() {
        failedAttempts = 0
        lockoutUntilTimestamp = 0L
    }

    fun isLockedOut(): Boolean {
        return System.currentTimeMillis() < lockoutUntilTimestamp
    }

    fun getRemainingLockoutSeconds(): Int {
        val remaining = (lockoutUntilTimestamp - System.currentTimeMillis()) / 1000
        return remaining.coerceAtLeast(0).toInt()
    }
}
