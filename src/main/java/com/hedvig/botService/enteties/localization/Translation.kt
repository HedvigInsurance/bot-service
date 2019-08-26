package com.hedvig.botService.enteties.localization

import com.hedvig.botService.enteties.localization.Key

data class Translation(
    val __typename: String,
    val key: Key,
    val text: String
)
