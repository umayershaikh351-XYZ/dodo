package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.crypto.CryptoService
import com.example.data.db.AppDatabase
import com.example.data.repository.DecryptedVaultEntry
import com.example.data.repository.SecurityPreferences
import com.example.data.repository.VaultRepository
import com.example.domain.model.DeviceIntelProfile
import com.example.domain.model.SecurityScoreResult
import com.example.domain.service.ClipboardService
import com.example.domain.service.DeviceIntelService
import com.example.domain.service.ScoreEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CipherLockViewModel(application: Application) : AndroidViewModel(application) {

    private val securityPrefs = SecurityPreferences(application)
    private val cryptoService = CryptoService()
    private val database = AppDatabase.getInstance(application)
    private val vaultRepository = VaultRepository(database.vaultDao(), cryptoService)
    private val intelService = DeviceIntelService(application)
    private val scoreEngine = ScoreEngine()
    val clipboardService = ClipboardService(application)

    // Lock and Auth states
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _hasPin = MutableStateFlow(securityPrefs.hasPin())
    val hasPin: StateFlow<Boolean> = _hasPin.asStateFlow()

    private val _isOnboardingDone = MutableStateFlow(securityPrefs.isOnboardingDone)
    val isOnboardingDone: StateFlow<Boolean> = _isOnboardingDone.asStateFlow()

    private val _lockoutSeconds = MutableStateFlow(securityPrefs.getRemainingLockoutSeconds())
    val lockoutSeconds: StateFlow<Int> = _lockoutSeconds.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Intel & Security
    private val _deviceProfile = MutableStateFlow<DeviceIntelProfile?>(null)
    val deviceProfile: StateFlow<DeviceIntelProfile?> = _deviceProfile.asStateFlow()

    private val _scoreResult = MutableStateFlow<SecurityScoreResult?>(null)
    val scoreResult: StateFlow<SecurityScoreResult?> = _scoreResult.asStateFlow()

    // Vault search and filtering
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("ALL")

    val vaultEntries: StateFlow<List<DecryptedVaultEntry>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                vaultRepository.allEntries
            } else {
                vaultRepository.searchEntries(query)
            }
        }
        .combine(selectedCategory) { list, category ->
            if (category == "ALL") list else list.filter { it.category == category }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    val autoLockSeconds = MutableStateFlow(securityPrefs.autoLockSeconds)
    val isSoundEnabled = MutableStateFlow(securityPrefs.isSoundEnabled)
    val isBiometricEnabled = MutableStateFlow(securityPrefs.isBiometricEnabled)

    private var lockoutCountdownJob: Job? = null
    private var lastBackgroundTimestamp = 0L

    init {
        checkLockoutTimer()
        runDeviceScan()
    }

    fun completeOnboarding() {
        securityPrefs.isOnboardingDone = true
        _isOnboardingDone.value = true
    }

    fun setupInitialPin(pin: String) {
        securityPrefs.setPin(pin)
        _hasPin.value = true
        _isUnlocked.value = true
        securityPrefs.resetFailedAttempts()
    }

    fun changePin(newPin: String) {
        securityPrefs.setPin(newPin)
        _hasPin.value = true
    }

    fun verifyPin(pin: String): Boolean {
        if (securityPrefs.isLockedOut()) {
            _lockoutSeconds.value = securityPrefs.getRemainingLockoutSeconds()
            return false
        }

        val success = securityPrefs.verifyPin(pin)
        if (success) {
            securityPrefs.resetFailedAttempts()
            _pinError.value = false
            _isUnlocked.value = true
            return true
        } else {
            _pinError.value = true
            val attempts = securityPrefs.recordFailedAttempt()
            if (attempts >= 3) {
                startLockoutCountdown()
            }
            return false
        }
    }

    fun unlockWithBiometrics() {
        if (!securityPrefs.isLockedOut()) {
            securityPrefs.resetFailedAttempts()
            _isUnlocked.value = true
            _pinError.value = false
        }
    }

    fun lockApp() {
        _isUnlocked.value = false
        clipboardService.clearOnAppBackground()
    }

    fun onAppForegrounded() {
        val now = System.currentTimeMillis()
        if (lastBackgroundTimestamp > 0L) {
            val elapsedSec = (now - lastBackgroundTimestamp) / 1000
            if (elapsedSec >= autoLockSeconds.value) {
                _isUnlocked.value = false
            }
        }
        checkLockoutTimer()
    }

    fun onAppBackgrounded() {
        lastBackgroundTimestamp = System.currentTimeMillis()
        clipboardService.clearOnAppBackground()
    }

    private fun checkLockoutTimer() {
        if (securityPrefs.isLockedOut()) {
            startLockoutCountdown()
        } else {
            _lockoutSeconds.value = 0
        }
    }

    private fun startLockoutCountdown() {
        lockoutCountdownJob?.cancel()
        lockoutCountdownJob = viewModelScope.launch {
            while (securityPrefs.isLockedOut()) {
                _lockoutSeconds.value = securityPrefs.getRemainingLockoutSeconds()
                delay(1000)
            }
            _lockoutSeconds.value = 0
            securityPrefs.resetFailedAttempts()
        }
    }

    fun runDeviceScan() {
        viewModelScope.launch(Dispatchers.Default) {
            _isScanning.value = true
            delay(400) // Brief animation time
            val profile = intelService.collectFullProfile()
            val score = scoreEngine.evaluate(profile)
            _deviceProfile.value = profile
            _scoreResult.value = score
            securityPrefs.lastScanTime = System.currentTimeMillis()
            _isScanning.value = false
        }
    }

    // Vault actions
    fun saveVaultEntry(
        id: Long = 0,
        title: String,
        username: String,
        password: String,
        notes: String,
        category: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            vaultRepository.saveEntry(
                id = id,
                title = title,
                username = username,
                password = password,
                notes = notes,
                category = category
            )
            onSuccess()
        }
    }

    suspend fun getVaultEntryById(id: Long): DecryptedVaultEntry? {
        return vaultRepository.getEntryById(id)
    }

    fun deleteVaultEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            vaultRepository.deleteEntry(id)
        }
    }

    fun clearAllVault() {
        viewModelScope.launch(Dispatchers.IO) {
            vaultRepository.clearAll()
        }
    }

    fun generateStrongPassword(): String {
        return cryptoService.generatePassword(16)
    }

    fun getPasswordStrength(password: String): Pair<Int, String> {
        return cryptoService.calculateStrength(password)
    }

    fun copyToClipboard(label: String, text: String) {
        clipboardService.copySecure(label, text, 30_000L)
    }

    // Settings adjustments
    fun setAutoLockTimer(seconds: Int) {
        securityPrefs.autoLockSeconds = seconds
        autoLockSeconds.value = seconds
    }

    fun setSound(enabled: Boolean) {
        securityPrefs.isSoundEnabled = enabled
        isSoundEnabled.value = enabled
    }

    fun setBiometric(enabled: Boolean) {
        securityPrefs.isBiometricEnabled = enabled
        isBiometricEnabled.value = enabled
    }
}
