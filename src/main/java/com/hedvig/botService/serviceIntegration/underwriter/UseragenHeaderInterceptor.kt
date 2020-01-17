package com.hedvig.botService.serviceIntegration.underwriter

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class UserAgentHeaderInterceptor : RequestInterceptor {

    private val userAgentHeader = "User-Agent"
    override fun apply(template: RequestTemplate?) {
        val requestAttributes =
            RequestContextHolder.getRequestAttributes() as ServletRequestAttributes? ?: return
        val request = requestAttributes.request
        val language = request.getHeader(userAgentHeader) ?: return
        template?.header(userAgentHeader, language)
    }
}
