package com.pega.constellation.sdk.kmp.ui.renderer.cmp.fields

import androidx.compose.runtime.Composable
import com.pega.constellation.sdk.kmp.core.api.Component
import com.pega.constellation.sdk.kmp.core.api.ComponentId
import com.pega.constellation.sdk.kmp.core.components.containers.SelectableCardComponent
import com.pega.constellation.sdk.kmp.core.components.fields.FieldComponent
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.SelectableCard
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.SelectableCardField
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.SelectableCardImage
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.SelectableCardImagePosition
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.SelectableCardImageSize
import com.pega.constellation.sdk.kmp.ui.components.cmp.controls.form.SelectableCardItem
import com.pega.constellation.sdk.kmp.ui.renderer.cmp.ComponentRenderer

class SelectableCardRenderer : ComponentRenderer<SelectableCardComponent> {
    @Composable
    override fun SelectableCardComponent.Render() {
        val childrenById = children.associateBy { it.context.id }
        SelectableCard(
            cards = cards.map { it.toSelectableCardItem(childrenById) },
            hideFieldLabels = hideFieldLabels,
            readOnly = readOnly,
            disabled = disabled,
            multiSelect = cardType == SelectableCardComponent.CardType.CHECKBOX,
            onCardClick = { index, selected -> onCardClick(index, selected) }
        )
    }

    private fun SelectableCardComponent.Card.toSelectableCardItem(
        childrenById: Map<ComponentId, Component>
    ) = SelectableCardItem(
        key = key,
        label = label,
        selected = selected,
        fields = createFields(childrenById),
        image = image?.createImage()
    )

    private fun SelectableCardComponent.Card.createFields(
        childrenById: Map<ComponentId, Component>
    ) = componentIds
        .mapNotNull { childrenById[ComponentId(it)] as? FieldComponent }
        .map { SelectableCardField(it.label, it.value) }

    private fun SelectableCardComponent.CardImage.createImage() = SelectableCardImage(
        src = src,
        alt = alt,
        position = SelectableCardImagePosition.valueOf(position.name),
        size = SelectableCardImageSize.valueOf(size.name)
    )
}
