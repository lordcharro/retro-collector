package com.retrocollector.app.wishlist.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.retrocollector.app.core.presentation.components.TactileTextField
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.wishlist.domain.usecase.ImportResult
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Modal Dialog for importing games into the Wishlist via CSV.
 * Supports multi-line input, preview of parsed games,
 * and displays AI enrichment progress.
 */
@Composable
fun ImportWishlistDialog(
    importResult: ImportResult?,
    enrichmentProgress: Pair<Int, Int>?,
    onImport: (String) -> Unit,
    onDismiss: () -> Unit,
    onResetImportResult: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var csvText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier
                .widthIn(min = 400.dp, max = 600.dp)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = TextKeys.Wishlist.IMPORT_DIALOG_TITLE,
                    style = HeadlineMd,
                    color = TextPrimary
                )

                // Format Hint
                Text(
                    text = TextKeys.Wishlist.IMPORT_FORMAT_HINT,
                    style = BodySm.copy(fontSize = 12.sp),
                    color = StatusUnverifiedFg
                )

                // Example
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBase, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Super Smash Bros. Melee, GameCube\nDead Space, PS3\nGoldenEye 007, N64",
                        style = CodeSkuStyle.copy(fontSize = 11.sp),
                        color = StatusUnverifiedFg
                    )
                }

                // Multiline text field for CSV
                TactileTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    placeholder = "Enter one game per line: title, platform",
                    singleLine = false,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )

                // Import Result (if present)
                if (importResult != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceElevated, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (importResult.added.isNotEmpty()) {
                            Text(
                                text = "${TextKeys.Wishlist.IMPORT_PREVIEW_READY} (${importResult.added.size})",
                                style = LabelBadgeStyle,
                                color = StatusEnglishFg
                            )
                        }
                        if (importResult.duplicates.isNotEmpty()) {
                            Text(
                                text = "${TextKeys.Wishlist.IMPORT_PREVIEW_DUPLICATE} (${importResult.duplicates.size})",
                                style = LabelBadgeStyle,
                                color = StatusEditionFg
                            )
                            importResult.duplicates.forEach { dup ->
                                Text(
                                    text = "  • $dup",
                                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                                    color = StatusUnverifiedFg
                                )
                            }
                        }
                        if (importResult.invalidLines.isNotEmpty()) {
                            Text(
                                text = "${TextKeys.Wishlist.IMPORT_PREVIEW_INVALID} (${importResult.invalidLines.size})",
                                style = LabelBadgeStyle,
                                color = StatusRiskFg
                            )
                            importResult.invalidLines.forEach { line ->
                                Text(
                                    text = "  • $line",
                                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                                    color = StatusUnverifiedFg
                                )
                            }
                        }
                    }
                }

                // Enrichment Progress
                if (enrichmentProgress != null) {
                    val (completed, total) = enrichmentProgress
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = ConsoleGamecube,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "${TextKeys.Wishlist.IMPORT_PROGRESS} $completed/$total",
                                style = BodySm.copy(fontSize = 12.sp),
                                color = ConsoleGamecube
                            )
                        }
                        LinearProgressIndicator(
                            progress = { if (total > 0) completed.toFloat() / total else 0f },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = ConsoleGamecube,
                            trackColor = SurfaceElevated
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    if (importResult != null) {
                        OutlinedButton(
                            onClick = {
                                csvText = ""
                                onResetImportResult()
                            },
                            border = BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = TextKeys.Wishlist.IMPORT_RESET_BUTTON,
                                style = LabelFilterStyle,
                                color = TextPrimary
                            )
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = TextKeys.Wishlist.IMPORT_FINISH_BUTTON,
                                style = LabelFilterStyle,
                                color = Color.White
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            border = BorderStroke(1.dp, BorderSubtle),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = TextKeys.Scanner.CLOSE,
                                style = LabelFilterStyle,
                                color = TextSecondary
                            )
                        }

                        val lineCount = remember(csvText) {
                            csvText.lines().count { it.trim().isNotBlank() }
                        }

                        Button(
                            onClick = { onImport(csvText) },
                            enabled = csvText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${TextKeys.Wishlist.IMPORT_BUTTON_LABEL} ($lineCount)",
                                style = LabelFilterStyle,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ImportWishlistDialogPreview() {
    RetroTactileTheme {
        ImportWishlistDialog(
            importResult = null,
            enrichmentProgress = null,
            onImport = {},
            onDismiss = {}
        )
    }
}
