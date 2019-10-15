package com.hedvig.botService.chat.house

import com.hedvig.botService.enteties.message.KeyboardType
import com.hedvig.botService.enteties.message.TextContentType

data class TextInputMessage(
    val id: String,
    val text: String,
    val textContentType: TextContentType,
    val keyboardType: KeyboardType,
    val placeholder: String
)
