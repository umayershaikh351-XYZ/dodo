package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.BootSequenceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceIntelScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SecurityScanScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupPinScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UnlockScreen
import com.example.ui.screens.VaultEntryScreen
import com.example.ui.screens.VaultListScreen
import com.example.ui.viewmodel.CipherLockViewModel

enum class Screen {
    SPLASH,
    ONBOARDING,
    SETUP_PIN,
    CHANGE_PIN,
    UNLOCK,
    BOOT_SEQUENCE,
    DASHBOARD,
    DEVICE_INTEL,
    SECURITY_SCAN,
    VAULT_LIST,
    VAULT_ENTRY,
    SETTINGS,
    ABOUT
}

@Composable
fun CipherLockApp(
    viewModel: CipherLockViewModel,
    onTriggerBiometric: (() -> Unit) -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var selectedVaultEntryId by remember { mutableStateOf(0L) }

    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val hasPin by viewModel.hasPin.collectAsState()
    val isOnboardingDone by viewModel.isOnboardingDone.collectAsState()
    val lockoutSeconds by viewModel.lockoutSeconds.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val deviceProfile by viewModel.deviceProfile.collectAsState()
    val scoreResult by viewModel.scoreResult.collectAsState()
    val vaultEntries by viewModel.vaultEntries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val autoLockSeconds by viewModel.autoLockSeconds.collectAsState()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

    // Handle back button on sub-screens
    if (currentScreen != Screen.DASHBOARD && currentScreen != Screen.UNLOCK && currentScreen != Screen.SPLASH) {
        BackHandler {
            when (currentScreen) {
                Screen.VAULT_ENTRY -> currentScreen = Screen.VAULT_LIST
                Screen.ABOUT -> currentScreen = Screen.SETTINGS
                Screen.CHANGE_PIN -> currentScreen = Screen.SETTINGS
                Screen.ONBOARDING -> {} // can't exit onboarding
                Screen.SETUP_PIN -> {}
                Screen.BOOT_SEQUENCE -> currentScreen = Screen.DASHBOARD
                else -> currentScreen = Screen.DASHBOARD
            }
        }
    }

    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(durationMillis = 200),
        modifier = Modifier.fillMaxSize(),
        label = "screenTransition"
    ) { screen ->
        when (screen) {
            Screen.SPLASH -> {
                SplashScreen(
                    onSplashFinished = {
                        currentScreen = when {
                            !isOnboardingDone -> Screen.ONBOARDING
                            !hasPin -> Screen.SETUP_PIN
                            !isUnlocked -> Screen.UNLOCK
                            else -> Screen.DASHBOARD
                        }
                    }
                )
            }

            Screen.ONBOARDING -> {
                OnboardingScreen(
                    onProceedToSetup = {
                        viewModel.completeOnboarding()
                        currentScreen = Screen.SETUP_PIN
                    }
                )
            }

            Screen.SETUP_PIN -> {
                SetupPinScreen(
                    onPinSetupComplete = { pin ->
                        viewModel.setupInitialPin(pin)
                        currentScreen = Screen.BOOT_SEQUENCE
                    }
                )
            }

            Screen.CHANGE_PIN -> {
                SetupPinScreen(
                    onPinSetupComplete = { newPin ->
                        viewModel.changePin(newPin)
                        currentScreen = Screen.SETTINGS
                    }
                )
            }

            Screen.UNLOCK -> {
                UnlockScreen(
                    lockoutSeconds = lockoutSeconds,
                    isBiometricAvailable = isBiometricEnabled,
                    onVerifyPin = { pin ->
                        viewModel.verifyPin(pin)
                    },
                    onBiometricRequested = {
                        onTriggerBiometric {
                            viewModel.unlockWithBiometrics()
                            currentScreen = Screen.BOOT_SEQUENCE
                        }
                    },
                    onUnlockSuccess = {
                        currentScreen = Screen.BOOT_SEQUENCE
                    }
                )
            }

            Screen.BOOT_SEQUENCE -> {
                BootSequenceScreen(
                    onBootFinished = {
                        currentScreen = Screen.DASHBOARD
                    }
                )
            }

            Screen.DASHBOARD -> {
                if (!isUnlocked) {
                    currentScreen = Screen.UNLOCK
                }
                DashboardScreen(
                    scoreResult = scoreResult,
                    deviceProfile = deviceProfile,
                    vaultItemCount = vaultEntries.size,
                    isScanning = isScanning,
                    onNavigateToIntel = { currentScreen = Screen.DEVICE_INTEL },
                    onNavigateToScan = { currentScreen = Screen.SECURITY_SCAN },
                    onNavigateToVault = { currentScreen = Screen.VAULT_LIST },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                    onRescanRequested = { viewModel.runDeviceScan() },
                    onLockRequested = {
                        viewModel.lockApp()
                        currentScreen = Screen.UNLOCK
                    }
                )
            }

            Screen.DEVICE_INTEL -> {
                DeviceIntelScreen(
                    profile = deviceProfile,
                    onNavigateBack = { currentScreen = Screen.DASHBOARD },
                    onRescan = { viewModel.runDeviceScan() },
                    onCopyToClipboard = { label, text ->
                        viewModel.copyToClipboard(label, text)
                    }
                )
            }

            Screen.SECURITY_SCAN -> {
                SecurityScanScreen(
                    scoreResult = scoreResult,
                    onNavigateBack = { currentScreen = Screen.DASHBOARD },
                    onRescan = { viewModel.runDeviceScan() }
                )
            }

            Screen.VAULT_LIST -> {
                VaultListScreen(
                    entries = vaultEntries,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    onSearchChanged = { viewModel.searchQuery.value = it },
                    onCategoryChanged = { viewModel.selectedCategory.value = it },
                    onNavigateToEntry = { id ->
                        selectedVaultEntryId = id
                        currentScreen = Screen.VAULT_ENTRY
                    },
                    onNavigateToCreate = {
                        selectedVaultEntryId = 0L
                        currentScreen = Screen.VAULT_ENTRY
                    },
                    onDeleteEntry = { id ->
                        viewModel.deleteVaultEntry(id)
                    },
                    onNavigateBack = { currentScreen = Screen.DASHBOARD }
                )
            }

            Screen.VAULT_ENTRY -> {
                VaultEntryScreen(
                    entryId = selectedVaultEntryId,
                    loadEntry = { id -> viewModel.getVaultEntryById(id) },
                    onGeneratePassword = { viewModel.generateStrongPassword() },
                    onCalculateStrength = { pass -> viewModel.getPasswordStrength(pass) },
                    onCopyToClipboard = { label, text -> viewModel.copyToClipboard(label, text) },
                    onSaveEntry = { id, title, user, pass, notes, cat ->
                        viewModel.saveVaultEntry(id, title, user, pass, notes, cat) {
                            currentScreen = Screen.VAULT_LIST
                        }
                    },
                    onDeleteEntry = { id ->
                        viewModel.deleteVaultEntry(id)
                        currentScreen = Screen.VAULT_LIST
                    },
                    onNavigateBack = { currentScreen = Screen.VAULT_LIST }
                )
            }

            Screen.SETTINGS -> {
                SettingsScreen(
                    autoLockSeconds = autoLockSeconds,
                    isSoundEnabled = isSoundEnabled,
                    isBiometricEnabled = isBiometricEnabled,
                    onAutoLockChanged = { viewModel.setAutoLockTimer(it) },
                    onSoundToggled = { viewModel.setSound(it) },
                    onBiometricToggled = { viewModel.setBiometric(it) },
                    onChangePinRequested = { currentScreen = Screen.CHANGE_PIN },
                    onRescanRequested = { viewModel.runDeviceScan() },
                    onClearAllVaultRequested = { viewModel.clearAllVault() },
                    onNavigateToAbout = { currentScreen = Screen.ABOUT },
                    onNavigateBack = { currentScreen = Screen.DASHBOARD }
                )
            }

            Screen.ABOUT -> {
                AboutScreen(
                    onNavigateBack = { currentScreen = Screen.SETTINGS }
                )
            }
        }
    }
}
