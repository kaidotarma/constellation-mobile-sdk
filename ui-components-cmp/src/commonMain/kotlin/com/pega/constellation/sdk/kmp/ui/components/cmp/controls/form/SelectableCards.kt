package com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.internal.HelperText

@Composable
fun SelectableCards(
    label: String,
    helperText: String,
    validateMessage: String,
    required: Boolean,
    disabled: Boolean,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Label(
            label = label,
            required = required,
            disabled = disabled,
            readOnly = readOnly
        )
        Spacer(Modifier.height(8.dp))
        content()
        HelperText(
            text = helperText,
            validateMessage = validateMessage,
            disabled = disabled,
            readOnly = readOnly
        )
    }
}
