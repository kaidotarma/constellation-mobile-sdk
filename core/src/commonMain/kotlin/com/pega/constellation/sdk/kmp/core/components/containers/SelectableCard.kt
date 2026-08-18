package com.pega.constellation.sdk.kmp.core.components.containers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pega.constellation.sdk.kmp.core.Log
import com.pega.constellation.sdk.kmp.core.api.ComponentContext
import com.pega.constellation.sdk.kmp.core.api.ComponentEvent
import com.pega.constellation.sdk.kmp.core.components.getJsonObject
import com.pega.constellation.sdk.kmp.core.components.getString
import com.pega.constellation.sdk.kmp.core.components.optBoolean
import com.pega.constellation.sdk.kmp.core.components.optJSONArray
import com.pega.constellation.sdk.kmp.core.components.optString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class SelectableCardComponent(context: ComponentContext) : ContainerComponent(context) {
    var value: String by mutableStateOf("")
        private set
    var disabled: Boolean by mutableStateOf(false)
        private set
    var readOnly: Boolean by mutableStateOf(false)
        private set
    var cardType: CardType by mutableStateOf(CardType.RADIO)
        private set
    var hideFieldLabels: Boolean by mutableStateOf(false)
        private set
    var cards: List<Card> by mutableStateOf(emptyList())
        private set

    override fun applyProps(props: JsonObject) {
        super.applyProps(props)
        value = props.optString("value")
        disabled = props.optBoolean("disabled", default = false)
        readOnly = props.optBoolean("readOnly", default = false)
        cardType = CardType.fromString(props.optString("cardType", default = "radio"))
        hideFieldLabels = props.optBoolean("hideFieldLabels", default = false)
        cards = props
            .optJSONArray("cards")
            ?.mapWithIndex { index -> getJsonObject(index) }
            ?.mapNotNull { createCard(it) }
            ?: emptyList()
    }

    private fun createCard(card: JsonObject): Card? {
        val common = card.getValue("commonProps").jsonObject
        val key = common.optString("key", "")
        if (key.isEmpty()) {
            Log.w(TAG, "Skipping card with empty 'key'.")
            return null
        }
        return Card(
            key = common.getString("key"),
            label = common.optString("label", ""),
            selected = common.optBoolean("selected", default = false),
            componentIds = card.optJSONArray("componentIds")
                ?.mapWithIndex { getString(it).toIntOrNull() }
                ?.filterNotNull()
                ?: emptyList(),
            image = card["image"]?.let { imageElement ->
                imageElement.jsonObject.let { image ->
                    CardImage(
                        src = image.optString("src"),
                        alt = image.optString("alt"),
                        position = CardImagePosition.fromString(image.optString("position")),
                        size = CardImageSize.fromString(image.optString("size"))
                    )
                }
            }
        )
    }

    fun onCardClick(itemIndex: Int, isSelected: Boolean) {
        context.sendComponentEvent(ComponentEvent.forItemClick(itemIndex, isSelected))
    }

    enum class CardType {
        RADIO, CHECKBOX;

        companion object {
            fun fromString(type: String): CardType {
                return entries.firstOrNull { it.name.equals(type, ignoreCase = true) } ?: RADIO
            }
        }
    }

    data class Card(
        val key: String,
        val label: String,
        val selected: Boolean,
        val componentIds: List<Int>,
        val image: CardImage?
    )

    enum class CardImagePosition {
        BEFORE_TEXT, ABOVE_TEXT, AFTER_TEXT;

        companion object {
            fun fromString(position: String): CardImagePosition {
                return when (position.lowercase()) {
                    "inline-start" -> BEFORE_TEXT
                    "block-start" -> ABOVE_TEXT
                    "inline-end" -> AFTER_TEXT
                    else -> ABOVE_TEXT
                }
            }
        }
    }

    enum class CardImageSize {
        SMALL, LARGE, FLEXIBLE;

        companion object {
            fun fromString(size: String): CardImageSize {
                return entries.firstOrNull { it.name.equals(size, ignoreCase = true) } ?: FLEXIBLE
            }
        }
    }

    data class CardImage(
        val src: String,
        val alt: String,
        val position: CardImagePosition,
        val size: CardImageSize
    )

    companion object {
        const val TAG = "SelectableCardComponent"
    }
}
