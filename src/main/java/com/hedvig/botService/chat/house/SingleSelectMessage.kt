package com.hedvig.botService.chat.house

data class SingleSelectMessage(
    val id: String,
    val text: String,
    val selectOptions: List<SingleSelectOption>
)
