package com.hedvig.botService.services.events

data class QuestionAskedEvent(
    var memberId: String,
    var question: String
)
