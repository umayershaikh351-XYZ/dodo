package com.example.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoService {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "CipherLock_MasterKey_AES256"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12 // 96-bit recommended for GCM
    }

    private val secureRandom = SecureRandom()

    init {
        ensureMasterKey()
    }

    private fun ensureMasterKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(parameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            ?: throw IllegalStateException("Keystore master key not found")
        return entry.secretKey
    }

    /**
     * Encrypts plaintext using AES-256-GCM hardware key.
     * Returns Pair(ciphertextBase64, ivBase64).
     */
    fun encrypt(plaintext: String): Pair<String, String> {
        if (plaintext.isEmpty()) return Pair("", "")
        val secretKey = getSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Pair(
            Base64.encodeToString(ciphertext, Base64.NO_WRAP),
            Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypts ciphertextBase64 with ivBase64 using AES-256-GCM.
     */
    fun decrypt(ciphertextBase64: String, ivBase64: String): String {
        if (ciphertextBase64.isEmpty() || ivBase64.isEmpty()) return ""
        return try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val ciphertext = Base64.decode(ciphertextBase64, Base64.NO_WRAP)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val plaintext = cipher.doFinal(ciphertext)
            String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            "[DECRYPTION FAILED]"
        }
    }

    /**
     * Generates a cryptographically strong 16-character password.
     */
    fun generatePassword(length: Int = 16): String {
        val upper = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val lower = "abcdefghijkmnopqrstuvwxyz"
        val digits = "23456789"
        val symbols = "!@#$%^&*()-_=+"
        val allChars = upper + lower + digits + symbols

        val sb = StringBuilder(length)
        // Ensure at least one from each pool
        sb.append(upper[secureRandom.nextInt(upper.length)])
        sb.append(lower[secureRandom.nextInt(lower.length)])
        sb.append(digits[secureRandom.nextInt(digits.length)])
        sb.append(symbols[secureRandom.nextInt(symbols.length)])

        for (i in 4 until length) {
            sb.append(allChars[secureRandom.nextInt(allChars.length)])
        }

        // Shuffle characters
        val list = sb.toList().shuffled(secureRandom)
        return list.joinToString("")
    }

    /**
     * Calculates password strength score (0 to 100) and grade.
     */
    fun calculateStrength(password: String): Pair<Int, String> {
        if (password.isEmpty()) return Pair(0, "EMPTY")
        var score = 0
        if (password.length >= 8) score += 20
        if (password.length >= 12) score += 20
        if (password.length >= 16) score += 15
        if (password.any { it.isUpperCase() }) score += 15
        if (password.any { it.isLowerCase() }) score += 10
        if (password.any { it.isDigit() }) score += 10
        if (password.any { "!@#$%^&*()-_=+".contains(it) }) score += 10

        val grade = when {
            score >= 80 -> "STRONG"
            score >= 50 -> "MEDIUM"
            else -> "WEAK"
        }
        return Pair(score.coerceIn(0, 100), grade)
    }
}
