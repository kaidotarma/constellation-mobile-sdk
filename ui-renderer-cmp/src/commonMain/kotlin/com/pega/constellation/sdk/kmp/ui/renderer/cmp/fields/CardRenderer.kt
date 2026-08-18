package com.pega.constellation.sdk.kmp.ui.renderer.cmp.fields

import androidx.compose.runtime.Composable
import com.pega.constellation.sdk.kmp.core.components.fields.CardFieldComponent
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.SelectableCards
import com.pega.constellation.sdk.kmp.ui.renderer.cmp.ComponentRenderer
import com.pega.constellation.sdk.kmp.ui.renderer.cmp.Render
import com.pega.constellation.sdk.kmp.ui.renderer.cmp.helpers.WithVisibility

abstract class CardRenderer : ComponentRenderer<CardFieldComponent> {
    @Composable
    override fun CardFieldComponent.Render() {
        WithVisibility(visible) {
            SelectableCards(
                label = label,
                helperText = helperText,
                validateMessage = validateMessage,
                required = required,
                disabled = disabled,
                readOnly = readOnly
            ) { cardComponent?.Render() }
        }
    }
}
