package com.back.p67260811.global.security

import com.back.p67260811.domain.member.entity.Member
import com.back.p67260811.domain.member.service.MemberService
import com.back.p67260811.global.exception.ServiceException
import com.back.p67260811.global.rq.Rq
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.function.Supplier

@Component
class CustomAuthenticationFilter(
    private val memberService: MemberService,
    private val rq: Rq
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            authenticate(request, response, filterChain)
        } catch (e: ServiceException) {
            val rsData = e.rsData
            response.run {
                contentType = "application/json; charset=UTF-8"
                status = rsData.statusCode
            }
            response.writer.write(
                """
                    {
                        "resultCode": "${rsData.resultCode}",
                        "msg": "${rsData.msg}"
                    }
                    """.trimIndent()
            )
        }
    }

    @Throws(ServletException::class, IOException::class)
    private fun authenticate(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
       if (!request.requestURI.startsWith("/api/")) {
           filterChain.doFilter(request, response)
           return
       }

       if (request.requestURI in listOf<String>(
               "/api/v1/members/join",
               "/api/v1/members/login")
       ) {
           filterChain.doFilter(request, response)
           return
       }


       val apiKey: String
       val accessToken: String

       val headerAuthorization = rq.getHeader("Authorization", "")

       if (headerAuthorization.isNotBlank()) {
           if (!headerAuthorization.startsWith("Bearer ")) throw ServiceException(
               "401-2",
               "Authorization 헤더가 Bearer 형식이 아닙니다."
           )

           val parts =
               headerAuthorization.split(" ", limit = 3)

           apiKey = parts[1]
           accessToken = parts.getOrElse(2) { "" }

       } else {
           apiKey = rq.getCookieValue("apiKey", "")
           accessToken = rq.getCookieValue("accessToken", "")
       }

        val isAccessTokenExists = accessToken.isNotBlank()
        val tokenMember = if (isAccessTokenExists) {
            memberService.payloadOrNull(accessToken)?.let { payload ->
                Member(
                    payload["id"] as Int,
                    payload["username"] as String?,
                    payload["nickname"] as String?
                )
            }
        } else {
            null
        }

        val member = tokenMember
            ?: memberService.findByApiKey(apiKey).orElseThrow {
                ServiceException("401-3", "API 키가 유효하지 않습니다.")
            }

        if (isAccessTokenExists && tokenMember == null) {
            val newAccessToken = memberService.genAccessToken(member)
            rq.addCookie("accessToken", newAccessToken)
            rq.setHeader("accessToken", newAccessToken)
        }

        val user = SecurityUser(
            id = member.id,
            nickname = member.nickname,
            username = member.username,
            password = member.password?: "",
            authorities = member.getAuthorities()
        )

        val authentication = UsernamePasswordAuthenticationToken(
            user,
            user.password,
            user.authorities
        )

        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }
}