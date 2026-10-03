package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DecryptedVaultEntry
import com.example.ui.components.ScanlineOverlay
import com.example.ui.components.TerminalButton
import com.example.ui.components.TerminalButtonType
import com.example.ui.components.TerminalFooter
import com.example.ui.components.TerminalPanel
import com.example.ui.components.TerminalStatusBar
import com.example.ui.theme.BackgroundElevated
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSurface
import com.example.ui.theme.BorderDefault
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusOk
import com.example.ui.theme.StatusWarn
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun VaultEntryScreen(
    entryId: Long,
    loadEntry: suspend (Long) -> DecryptedVaultEntry?,
    onGeneratePassword: () -> String,
    onCalculateStrength: (String) -> Pair<Int, String>,
    onCopyToClipboard: (String, String) -> Unit,
    onSaveEntry: (Long, String, String, String, String, String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ACCOUNT") }
    var showPassword by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        if (entryId > 0L && !isInitialized) {
            val entry = loadEntry(entryId)
            if (entry != null) {
                title = entry.title
                username = entry.username
                password = entry.password
                notes = entry.notes
                category = entry.category
            }
            isInitialized = true
        }
    }

    val (strengthScore, strengthLabel) = remember(password) {
        onCalculateStrength(password)
    }

    val strengthColor = when (strengthLabel) {
        "STRONG" -> StatusOk
        "MEDIUM" -> StatusWarn
        else -> StatusFail
    }

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (entryId > 0L) "> EDIT VAULT RECORD" else "> NEW VAULT RECORD",
                            fontFamily = TerminalFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            text = "AES-256-GCM / ZERO STORAGE IN PLAINTEXT",
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            color = MatrixGreenDim
                        )
                    }
                    TerminalButton(
                        text = "[ CANCEL ]",
                        onClick = onNavigateBack,
                        type = TerminalButtonType.SECONDARY,
                        testTag = "vault_entry_cancel_btn"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category selector
                Text(
                    text = "> CATEGORY:",
                    fontFamily = TerminalFontFamily,
                    fontSize = 11.sp,
                    color = MatrixGreenDim,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                val categories = listOf("ACCOUNT", "PIN", "WIFI", "NOTE", "KEY")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) MatrixGreen else BackgroundSurface)
                                .border(1.dp, if (isSelected) MatrixGreen else BorderDefault, RoundedCornerShape(3.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                fontFamily = TerminalFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BackgroundPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Field 1: Title
                TerminalInputField(
                    label = "TITLE / SERVICE",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "e.g. Gmail / Work VPN / Banking PIN",
                    testTag = "input_vault_title"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Field 2: Username / Identifier
                TerminalInputField(
                    label = "USERNAME / IDENTIFIER",
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "username or access handle",
                    testTag = "input_vault_username"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Field 3: Password / Secret
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "> SECRET / KEY:",
                            fontFamily = TerminalFontFamily,
                            fontSize = 11.sp,
                            color = MatrixGreenDim,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            Text(
                                text = if (showPassword) "[ HIDE ]" else "[ SHOW ]",
                                fontFamily = TerminalFontFamily,
                                fontSize = 10.sp,
                                color = StatusInfo,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showPassword = !showPassword }
                                    .padding(end = 12.dp)
                            )
                            Text(
                                text = "[ GENERATE 16 ]",
                                fontFamily = TerminalFontFamily,
                                fontSize = 10.sp,
                                color = MatrixGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { password = onGeneratePassword() }
                                    .padding(end = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundSurface)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BasicTextField(
                                value = password,
                                onValueChange = { password = it },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                textStyle = TextStyle(
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                ),
                                cursorBrush = SolidColor(MatrixGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_vault_password"),
                                decorationBox = { innerTextField ->
                                    if (password.isEmpty()) {
                                        Text(
                                            text = "enter secret or click generate...",
                                            fontFamily = TerminalFontFamily,
                                            fontSize = 12.sp,
                                            color = TextTertiary
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (password.isNotEmpty()) {
                                Text(
                                    text = "[ COPY ]",
                                    fontFamily = TerminalFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixGreen,
                                    modifier = Modifier.clickable {
                                        onCopyToClipboard("Secret", password)
                                        Toast.makeText(context, "SECRET COPIED (CLEARS IN 30s)", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MatrixGreen.copy(alpha = 0.6f))
                                .align(Alignment.BottomCenter)
                        )
                    }

                    if (password.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "STRENGTH: $strengthLabel ($strengthScore%)",
                                fontFamily = TerminalFontFamily,
                                fontSize = 10.sp,
                                color = strengthColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ENTROPY: ${password.length * 4} BITS",
                                fontFamily = TerminalFontFamily,
                                fontSize = 10.sp,
                                color = TextTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Field 4: Notes
                TerminalInputField(
                    label = "SECURE MEMO / NOTES",
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "recovery codes, seed words, endpoint tokens...",
                    testTag = "input_vault_notes",
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                TerminalButton(
                    text = "[ SAVE HARDWARE-ENCRYPTED ]",
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "TITLE REQUIRED", Toast.LENGTH_SHORT).show()
                        } else {
                            onSaveEntry(entryId, title, username, password, notes, category)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    type = TerminalButtonType.PRIMARY,
                    testTag = "save_vault_entry_btn"
                )

                if (entryId > 0L) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TerminalButton(
                        text = "[ DELETE RECORD ]",
                        onClick = { onDeleteEntry(entryId) },
                        modifier = Modifier.fillMaxWidth(),
                        type = TerminalButtonType.DANGER,
                        testTag = "delete_vault_entry_btn"
                    )
                }
            }

            TerminalFooter()
        }
    }
}

@Composable
private fun TerminalInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String,
    singleLine: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "> $label:",
            fontFamily = TerminalFontFamily,
            fontSize = 11.sp,
            color = MatrixGreenDim,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else 4,
                textStyle = TextStyle(
                    fontFamily = TerminalFontFamily,
                    fontSize = 13.sp,
                    color = TextPrimary
                ),
                cursorBrush = SolidColor(MatrixGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                    innerTextField()
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MatrixGreen.copy(alpha = 0.6f))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
