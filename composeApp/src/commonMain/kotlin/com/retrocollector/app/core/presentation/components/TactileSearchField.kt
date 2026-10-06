package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TactileSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.dashboard_search_placeholder),
    onSearchSubmit: ((String) -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    searchIcon: String = "🔍",
    backgroundColor: Color = SurfaceBase,
    focusedBorderColor: Color = AccentBlue,
    unfocusedBorderColor: Color = BorderSubtle,
    shape: Shape = RoundedCornerShape(4.dp),
    textStyle: TextStyle = CodeSkuStyle.copy(color = TextPrimary),
    placeholderStyle: TextStyle = CodeSkuStyle.copy(fontSize = 11.sp, color = StatusUnverifiedFg),
    minHeight: Dp = 32.dp
) {
    TactileTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = placeholder,
        singleLine = true,
        textStyle = textStyle,
        placeholderStyle = placeholderStyle,
        backgroundColor = backgroundColor,
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        shape = shape,
        minHeight = minHeight,
        keyboardOptions = KeyboardOptions(
            imeAction = if (onSearchSubmit != null) ImeAction.Search else ImeAction.Default
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchSubmit?.invoke(query) }
        ),
        leadingIcon = {
            Text(text = searchIcon, fontSize = 12.sp)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Text(
                    text = "✕",
                    style = BodySm.copy(fontSize = 11.sp, color = StatusUnverifiedFg),
                    modifier = Modifier
                        .clickable { onQueryChange("") }
                        .padding(horizontal = 4.dp)
                )
            }
            trailingContent?.invoke()
        }
    )
}

@Preview
@Composable
fun TactileSearchFieldPreview() {
    RetroTactileTheme {
        TactileSearchField(
            query = "Resident Evil",
            onQueryChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
