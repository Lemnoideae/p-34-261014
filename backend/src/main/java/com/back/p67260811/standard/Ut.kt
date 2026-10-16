package com.back.p67260811.standard

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date

object Ut {
    @Suppress("ClassName")
    object jwt {
        @JvmStatic
        fun toString(secret: String, expireSeconds: Long, body: Map<String, Any?>): String {
            val issuedAt = Date()
            val expiration = Date(issuedAt.time + expireSeconds)
            val secretKey = Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))

            return Jwts.builder()
                .claims(body)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()
        }

        @JvmStatic
        fun isValid(jwt: String, secretPattern: String): Boolean =
            payloadOrNull(jwt, secretPattern) != null

        @JvmStatic
        fun payloadOrNull(jwt: String, secretPattern: String): Map<String, Any>? =
            try {
                val secretKey = Keys.hmacShaKeyFor(secretPattern.toByteArray(Charsets.UTF_8))

                Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .payload
            } catch (_: Exception) {
                null
            }
    }
}
