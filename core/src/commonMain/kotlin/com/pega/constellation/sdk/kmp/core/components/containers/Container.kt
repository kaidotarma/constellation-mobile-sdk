package com.pega.constellation.sdk.kmp.core.components.containers

import androidx.annotation.CallSuper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pega.constellation.sdk.kmp.core.api.BaseComponent
import com.pega.constellation.sdk.kmp.core.api.Component
import com.pega.constellation.sdk.kmp.core.api.ComponentContext
import com.pega.constellation.sdk.kmp.core.api.ComponentId
import com.pega.constellation.sdk.kmp.core.api.ComponentType
import com.pega.constellation.sdk.kmp.core.components.getString
import com.pega.constellation.sdk.kmp.core.components.optJSONArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

abstract class ContainerComponent(context: ComponentContext) : BaseComponent(context) {
    var children: List<Component> by mutableStateOf(emptyList())
        private set

    @CallSuper
    override fun applyProps(props: JsonObject) {
        children = adoptChildrenAndGet(props)
    }

    private fun adoptChildrenAndGet(props: JsonObject): List<Component> =
        props.getChildren()?.let { children ->
            val ids = children.map { it.id }
            ids.map { ComponentId(it) }.mapNotNull { adoptChildAndGet(it) }
        } ?: emptyList()
}

data class Child(val id: Int, val componentType: ComponentType)

private fun JsonObject.getChildren() = optJSONArray("children")?.map {
    Child(it.jsonObject.getString("id").toInt(), ComponentType(it.jsonObject.getString("type")))
}
