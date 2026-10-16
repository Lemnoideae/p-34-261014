package com.back.p67260811.domain.member.service

import com.back.p67260811.domain.member.entity.Member
import com.back.p67260811.standard.Ut
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthTokenService {
    @Value("\${custom.jwt.secret-key}")
    private lateinit var secretPattern: String

    @Value("\${custom.jwt.expireMills}")
    private val expireMills: Long = 0

    fun genAccessToken(member: Member): String {
        return Ut.jwt.toString(
            secretPattern,
            expireMills,
            mapOf(
                "id" to member.id,
                "username" to member.username,
                "nickname" to member.nickname
            )
        )
    }

    fun payloadOrNull(jwt: String): Map<String, Any>? {
        val payload = Ut.jwt.payloadOrNull(jwt, secretPattern) ?: return null

        return mapOf(
            "id" to (payload["id"] as Number).toInt(),
            "username" to payload["username"] as String,
            "nickname" to payload["nickname"] as String
        )
    }
}