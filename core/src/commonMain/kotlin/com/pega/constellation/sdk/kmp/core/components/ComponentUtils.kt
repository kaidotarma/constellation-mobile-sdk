package com.pega.constellation.sdk.kmp.core.components

import com.pega.constellation.sdk.kmp.core.api.Component
import com.pega.constellation.sdk.kmp.core.components.containers.ContainerComponent

fun Component.structure(indent: String = ""): String {
    val self = indent + this + "\n"
    val children = children().joinToString("") { it.structure("$indent-") }
    return self + children
}

fun Component.children() = (this as? ContainerComponent)?.children ?: emptyList()