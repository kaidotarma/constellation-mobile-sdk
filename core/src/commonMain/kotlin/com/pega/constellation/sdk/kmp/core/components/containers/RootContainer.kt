package com.pega.constellation.sdk.kmp.core.components.containers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pega.constellation.sdk.kmp.core.api.ComponentContext
import com.pega.constellation.sdk.kmp.core.components.getJsonObject
import com.pega.constellation.sdk.kmp.core.components.getString
import com.pega.constellation.sdk.kmp.core.components.optJSONArray
import com.pega.constellation.sdk.kmp.core.components.widgets.Dialog
import kotlinx.serialization.json.JsonObject

class RootContainerComponent(context: ComponentContext) : ContainerComponent(context) {
    var httpMessages: List<String> by mutableStateOf(emptyList())
        private set
    var dialogConfig: Dialog.Config? by mutableStateOf(null)
        private set

    fun presentDialog(config: Dialog.Config) {
        dialogConfig = config
        notifyObservers()
    }

    fun dismissDialog() {
        dialogConfig = null
        notifyObservers()
    }

    override fun applyProps(props: JsonObject) {
        super.applyProps(props)
        httpMessages = props.optJSONArray("httpMessages")?.mapWithIndex {
            val httpMessage = getJsonObject(it)
            val type = httpMessage.getString("type")
            val message = httpMessage.getString("message")
            val prefix = if (type == "error") "Http error: " else ""
            prefix + message
        } ?: emptyList()
    }

    fun clearMessages() {
        httpMessages = emptyList()
    }
}
