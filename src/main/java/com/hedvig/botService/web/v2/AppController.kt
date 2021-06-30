package com.hedvig.botService.web.v2

import com.hedvig.botService.enteties.UserContextRepository
import com.hedvig.botService.serviceIntegration.notificationService.NotificationService
import com.hedvig.botService.services.MessagesService
import com.hedvig.botService.services.SessionManager
import com.hedvig.botService.enteties.message.Message

import com.hedvig.botService.web.v2.dto.*
import com.hedvig.libs.translations.Translations

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.transaction.Transactional

@RestController
@RequestMapping("/v2/app")
class AppController(
    private val messagesService: MessagesService,
    private val notificationService: NotificationService,
    private val userContextRepository: UserContextRepository,
    private val translations: Translations
) {

    @GetMapping("/")
    fun getMessages(
        @RequestHeader("hedvig.token") hid: String,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @RequestParam(name = "intent", required = false, defaultValue = "onboarding")
        intentParameter: String
    ): MessagesDTO {

        val intent = if (intentParameter == "login")
            SessionManager.Intent.LOGIN
        else
            SessionManager.Intent.ONBOARDING

        return this.messagesService.getMessagesAndStatus(hid, acceptLanguage, intent)
    }

    @PostMapping("fabTrigger/{actionId}")
    fun fabTrigger(
        @RequestHeader("hedvig.token") hid: String, @PathVariable actionId: FABAction,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?
    ): ResponseEntity<*> {

        return this.messagesService.fabTrigger(hid, acceptLanguage, actionId)
    }

    @PostMapping("/push-token")
    fun pushToken(
        @RequestBody dto: RegisterPushTokenRequest,
        @RequestHeader(value = "hedvig.token") memberId: String
    ): ResponseEntity<Void> {
        notificationService.setFirebaseToken(memberId, dto.token)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/markAsRead")
    fun markAsRead(
        @RequestBody dto: MarkAsReadRequest,
        @RequestHeader(value = "hedvig.token") memberId: String
    ): ResponseEntity<Message> {
        return ResponseEntity.ok(messagesService.markAsRead(memberId, dto.globalId))
    }

    @GetMapping("/onboarding-data")
    @Transactional
    fun onboardingData(@RequestHeader("hedvig.token") memberId: String): ResponseEntity<OnboardingData> {
        val userContext = userContextRepository
            .findByMemberId(memberId)

        if (!userContext.isPresent) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(OnboardingData.from(userContext.get()))
    }
}
