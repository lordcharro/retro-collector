package com.retrocollector.app.wishlist.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.wishlist.domain.usecase.ImportResult

/**
 * Dialog modal para importação de jogos na Wishlist via CSV.
 * Suporta input multilinha, preview dos jogos parseados,
 * e mostra progresso de enriquecimento com IA.
 */
@Composable
fun ImportWishlistDialog(
    importResult: ImportResult?,
    enrichmentProgress: Pair<Int, Int>?,
    onImport: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var csvText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

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
                // Título
                Text(
                    text = TextKeys.Wishlist.IMPORT_DIALOG_TITLE,
                    style = HeadlineMd,
                    color = TextPrimary
                )

                // Dica de formato
                Text(
                    text = TextKeys.Wishlist.IMPORT_FORMAT_HINT,
                    style = BodySm.copy(fontSize = 12.sp),
                    color = StatusUnverifiedFg
                )

                // Exemplo
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

                // Campo de texto multilinha para CSV
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(SurfaceBase, RoundedCornerShape(6.dp))
                        .border(1.dp, if (isFocused) AccentBlue else BorderSubtle, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = csvText,
                        onValueChange = { csvText = it },
                        textStyle = BodyMd.copy(color = TextPrimary),
                        modifier = Modifier
                            .fillMaxSize()
                            .onFocusChanged { isFocused = it.isFocused },
                        decorationBox = { innerTextField ->
                            Box {
                                if (csvText.isEmpty() && !isFocused) {
                                    Text(
                                        text = "Enter one game per line: title, platform",
                                        style = BodySm.copy(color = StatusUnverifiedFg)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Resultado da importação (se existir)
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

                // Progresso de enriquecimento
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

                // Botões de ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
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
                        enabled = csvText.isNotBlank() && importResult == null,
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
