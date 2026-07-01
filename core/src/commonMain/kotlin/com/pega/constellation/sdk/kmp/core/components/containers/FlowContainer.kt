package com.pega.constellation.sdk.kmp.core.components.containers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pega.constellation.sdk.kmp.core.api.ComponentContext
import com.pega.constellation.sdk.kmp.core.components.getString
import kotlinx.serialization.json.JsonObject

class FlowContainerComponent(context: ComponentContext) : ContainerComponent(context) {
    var title: String by mutableStateOf("")
        private set

    override fun applyProps(props: JsonObject) {
        super.applyProps(props)
        title = props.getString("title")
    }
}
