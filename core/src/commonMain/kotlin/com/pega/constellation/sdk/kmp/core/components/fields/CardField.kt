package com.pega.constellation.sdk.kmp.core.components.fields

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pega.constellation.sdk.kmp.core.api.ComponentContext
import com.pega.constellation.sdk.kmp.core.api.ComponentId
import com.pega.constellation.sdk.kmp.core.components.containers.SelectableCardComponent
import com.pega.constellation.sdk.kmp.core.components.optBoolean
import com.pega.constellation.sdk.kmp.core.components.optString
import kotlinx.serialization.json.JsonObject

abstract class CardFieldComponent(context: ComponentContext) : FieldComponent(context) {
    var cardComponent: SelectableCardComponent? by mutableStateOf(null)
        private set
    var inlineDisplay: Boolean by mutableStateOf(false)
        private set

    override fun applyProps(props: JsonObject) {
        super.applyProps(props)
        cardComponent = props.optString("cardComponentId").takeIf { it.isNotEmpty() }?.let {
            adoptChildAndGet(ComponentId(it.toInt())) as? SelectableCardComponent
        }
        inlineDisplay = props.optBoolean("inlineDisplay", true)
    }
}
