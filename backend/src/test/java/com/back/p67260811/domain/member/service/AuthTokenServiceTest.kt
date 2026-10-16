package com.back.p67260811.domain.member.service

import com.back.p67260811.domain.member.entity.Member
import com.back.p67260811.domain.member.repository.MemberRepository
import com.back.p67260811.standard.Ut
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthTokenServiceTest {

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Value($$"${custom.jwt.secret-key}")
    private lateinit var secretPattern: String

    @Value($$"${custom.jwt.expireMills}")
    private var expireMills: Long = 0L

    @Test
    @DisplayName("authTokenService 서비스가 존재한다.")
    fun t1() {
        assertThat(authTokenService).isNotNull()
    }

    @Test
    @DisplayName("jjwt 최신 방식으로 JWT 생성, {name=\"Paul\", age=23}")
    fun t2() {
        val payload = mapOf("name" to "Paul", "age" to 23)

        val jwt = Ut.jwt.toString(
            secretPattern,
            expireMills,
            payload
        )

        assertThat(jwt).isNotBlank()

        val parsedPayload = Ut.jwt.payloadOrNull(jwt, secretPattern)

        assertThat(parsedPayload)
            .containsAllEntriesOf(payload)

        println("jwt = $jwt")
    }

    @Test
    @DisplayName("Ut.jwt.toString 를 통해서 JWT 생성, {name=\"Paul\", age=23}")
    fun t3() {
        val payload = mapOf("name" to "Paul", "age" to 23)

        val jwt = Ut.jwt.toString(
            secretPattern,
            expireMills,
            payload
        )

        assertThat(jwt).isNotBlank()

        val validResult = Ut.jwt.isValid(jwt, secretPattern)
        assertThat(validResult).isTrue()

        val parsedPayload = Ut.jwt.payloadOrNull(jwt, secretPattern)

        assertThat(parsedPayload)
            .containsAllEntriesOf(payload)

        println("jwt = $jwt")
    }

    @Test
    @DisplayName("AuthTokenService를 통해서 accessToken 생성")
    fun t4() {
        val member1 = memberRepository.saveAndFlush(
            Member("auth-token-test", null, "Auth Token Test", "auth-token-test")
        )
        val accessToken = authTokenService.genAccessToken(member1)
        assertThat(accessToken).isNotBlank()

        val payload = authTokenService.payloadOrNull(accessToken)

        assertThat(payload).containsAllEntriesOf(
            mapOf(
                "id" to member1.id,
                "username" to member1.username,
                "nickname" to member1.nickname
            )
        )

        println("accessToken = $accessToken")
    }

    @Test
    @DisplayName("잘못된 accessToken의 payload는 null이다.")
    fun t5() {
        assertThat(authTokenService.payloadOrNull("not-a-jwt")).isNull()
    }

    @Test
    @DisplayName("만료된 accessToken의 payload는 null이다.")
    fun t6() {
        val expiredToken = Ut.jwt.toString(
            secretPattern,
            -1,
            mapOf("id" to 1, "username" to "expired", "nickname" to "Expired")
        )

        assertThat(authTokenService.payloadOrNull(expiredToken)).isNull()
    }
}
