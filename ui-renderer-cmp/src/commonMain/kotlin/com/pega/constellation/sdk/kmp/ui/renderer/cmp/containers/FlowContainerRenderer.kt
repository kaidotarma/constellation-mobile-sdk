package com.pega.constellation.sdk.kmp.ui.renderer.cmp.containers

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.pega.constellation.sdk.kmp.core.components.containers.FlowContainerComponent
import com.pega.constellation.sdk.kmp.core.components.containers.AssignmentComponent
import com.pega.constellation.sdk.kmp.core.components.widgets.AlertBannerComponent
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.common.Heading
import com.pega.constellation.sdk.kmp.ui.renderer.cmp.ComponentRenderer
import com.pega.constellation.sdk.kmp.ui.renderer.cmp.Render

class FlowContainerRenderer : ComponentRenderer<FlowContainerComponent> {
    @Composable
    override fun FlowContainerComponent.Render() {
        Column {
            Heading(title)
            children.filterIsInstance<AlertBannerComponent>().forEach { it.Render() }
            children.filterIsInstance<AssignmentComponent>().forEach { it.Render() }
        }
    }
}
