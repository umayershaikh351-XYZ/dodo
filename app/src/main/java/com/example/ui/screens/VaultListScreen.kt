package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.GridLine
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MatrixGreenDim
import com.example.ui.theme.StatusFail
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.TerminalFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun VaultListScreen(
    entries: List<DecryptedVaultEntry>,
    searchQuery: String,
    selectedCategory: String,
    onSearchChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onNavigateToEntry: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    var itemToDelete by remember { mutableStateOf<DecryptedVaultEntry?>(null) }

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
                            text = "> ENCRYPTED VAULT",
                            fontFamily = TerminalFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatrixGreen,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            text = "AES-256-GCM HARDWARE SEALED [${entries.size} ITEMS]",
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            color = MatrixGreenDim
                        )
                    }
                    TerminalButton(
                        text = "[ BACK ]",
                        onClick = onNavigateBack,
                        type = TerminalButtonType.SECONDARY,
                        testTag = "vault_back_btn"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Input with bottom border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "> SEARCH: ",
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            color = MatrixGreenDim,
                            fontWeight = FontWeight.Bold
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChanged,
                            textStyle = TextStyle(
                                fontFamily = TerminalFontFamily,
                                fontSize = 13.sp,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(MatrixGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vault_search_field"),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "title / username / notes...",
                                        fontFamily = TerminalFontFamily,
                                        fontSize = 12.sp,
                                        color = TextTertiary
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                    // Bottom border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MatrixGreen.copy(alpha = 0.6f))
                            .align(Alignment.BottomCenter)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                val categories = listOf("ALL", "ACCOUNT", "PIN", "WIFI", "NOTE", "KEY")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) MatrixGreen else BackgroundSurface)
                                .border(1.dp, if (isSelected) MatrixGreen else BorderDefault, RoundedCornerShape(3.dp))
                                .clickable { onCategoryChanged(cat) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                fontFamily = TerminalFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BackgroundPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List or Empty State
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "[  SECURED VAULT EMPTY  ]",
                                fontFamily = TerminalFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreenDim
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "> NO MATCHING CREDENTIAL ENTRIES FOUND",
                                fontFamily = TerminalFontFamily,
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TerminalButton(
                                text = "[ + CREATE FIRST ENTRY ]",
                                onClick = onNavigateToCreate,
                                type = TerminalButtonType.PRIMARY,
                                testTag = "create_first_entry_btn"
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        itemsIndexed(entries, key = { _, item -> item.id }) { index, item ->
                            val itemIndex = String.format("%02d", index + 1)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("vault_item_${item.id}")
                                    .clickable { onNavigateToEntry(item.id) }
                                    .padding(vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "[$itemIndex]",
                                            fontFamily = TerminalFontFamily,
                                            fontSize = 12.sp,
                                            color = MatrixGreenDim,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.title,
                                                fontFamily = TerminalFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (item.username.isNotEmpty()) {
                                                Text(
                                                    text = item.username,
                                                    fontFamily = TerminalFontFamily,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "[${item.category}]",
                                            fontFamily = TerminalFontFamily,
                                            fontSize = 10.sp,
                                            color = StatusInfo
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "> ACCESS",
                                            fontFamily = TerminalFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MatrixGreen
                                        )
                                    }
                                }
                            }
                            // 1dp Divider Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(GridLine)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TerminalButton(
                        text = "[ + ADD NEW ENTRY ]",
                        onClick = onNavigateToCreate,
                        modifier = Modifier.fillMaxWidth(),
                        type = TerminalButtonType.PRIMARY,
                        testTag = "add_vault_entry_btn"
                    )
                }
            }

            TerminalFooter()
        }

        // Delete Confirm Dialog
        if (itemToDelete != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundPrimary.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                TerminalPanel(
                    modifier = Modifier.padding(24.dp),
                    borderColor = StatusFail
                ) {
                    Column {
                        Text(
                            text = "> DELETE VAULT ENTRY",
                            fontFamily = TerminalFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusFail
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Permanently purge encrypted payload for \"${itemToDelete?.title}\"? This action cannot be undone.",
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TerminalButton(
                                text = "CANCEL",
                                onClick = { itemToDelete = null },
                                type = TerminalButtonType.SECONDARY
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TerminalButton(
                                text = "PURGE",
                                onClick = {
                                    itemToDelete?.let { onDeleteEntry(it.id) }
                                    itemToDelete = null
                                },
                                type = TerminalButtonType.DANGER
                            )
                        }
                    }
                }
            }
        }
    }
}
