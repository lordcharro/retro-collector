package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TactileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = BodyMd.copy(color = TextPrimary),
    placeholderStyle: TextStyle = BodySm.copy(color = StatusUnverifiedFg),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    backgroundColor: Color = SurfaceBase,
    focusedBorderColor: Color = AccentBlue,
    unfocusedBorderColor: Color = BorderSubtle,
    shape: Shape = RoundedCornerShape(4.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    minHeight: Dp = 34.dp
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) focusedBorderColor else unfocusedBorderColor

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .padding(contentPadding)
            .onFocusChanged { isFocused = it.isFocused },
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        leadingIcon?.invoke()

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            if (value.isEmpty() && !isFocused && placeholder != null) {
                Text(
                    text = placeholder,
                    style = placeholderStyle,
                    maxLines = if (singleLine) 1 else Int.MAX_VALUE
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                readOnly = readOnly,
                textStyle = textStyle,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                cursorBrush = SolidColor(AccentBlue)
            )
        }

        trailingIcon?.invoke()
    }
}

@Preview
@Composable
fun TactileTextFieldPreview() {
    RetroTactileTheme {
        TactileTextField(
            value = "Resident Evil 4",
            onValueChange = {},
            placeholder = "Nome do jogo...",
            modifier = Modifier.padding(16.dp)
        )
    }
}
