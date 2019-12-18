package com.hedvig.botService.web.v2

import com.hedvig.botService.enteties.UserContextRepository
import com.hedvig.botService.serviceIntegration.notificationService.NotificationService
import com.hedvig.botService.web.v2.dto.OnboardingData
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/_/v2/")
class InternalController(
    private val notificationService: NotificationService
) {
    @GetMapping("{memberId}/push-token")
    fun pushToken(@PathVariable memberId: String): ResponseEntity<String> {
        val possibleToken = notificationService.getFirebaseToken(memberId)

        return possibleToken
            .map { ResponseEntity.ok(it) }
            .orElseGet { ResponseEntity.notFound().build() }
    }
}
