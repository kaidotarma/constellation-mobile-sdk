package com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

const val TEXT_ALPHA = 0.38f

@Composable
fun getTextColor(disabled: Boolean) =
    when {
        disabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = TEXT_ALPHA)
        else -> MaterialTheme.colorScheme.onSurface
    }
