package com.hedvig.botService.serviceIntegration.localization

data class GraphQLQueryWrapper(
    val query: String,
    val variables: String? = null
)
