package com.back.p67260811.domain.member.controller

import com.back.p67260811.domain.member.repository.MemberRepository
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("회원 가입")
    fun t1() {
        val username = "newUser"
        val password = "1234"
        val nickname = "새유저"

        val resultActions = mvc.perform(
            post("/api/v1/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "$username",
                        "password": "$password",
                        "nickname": "$nickname"
                    }
                """.trimIndent())
        ).andDo(print())

        val member = memberRepository.findByUsername(username).orElseThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("join"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resultCode").value("201-1"))
            .andExpect(jsonPath("$.msg").value("회원가입이 완료되었습니다. ${nickname}님 환영합니다."))
            .andExpect(jsonPath("$.data.memberDto.id").value(member.id))
            .andExpect(jsonPath("$.data.memberDto.createDate").exists())
            .andExpect(jsonPath("$.data.memberDto.modifyDate").exists())
            .andExpect(jsonPath("$.data.memberDto.name").value(nickname))
    }

    @Test
    @DisplayName("회원 가입, 이미 존재하는 username으로 가입 - user1로 가입")
    fun t2() {
        val username = "user1"
        val password = "1234"
        val nickname = "새유저"

        val resultActions = mvc.perform(
            post("/api/v1/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "$username",
                        "password": "$password",
                        "nickname": "$nickname"
                    }
                """.trimIndent())
        ).andDo(print())

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("join"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.resultCode").value("409-1"))
            .andExpect(jsonPath("$.msg").value("이미 사용중인 아이디입니다."))
    }

    @Test
    @DisplayName("로그인")
    fun t3() {
        val username = "user1"
        val password = "1234"

        val resultActions = mvc.perform(
            post("/api/v1/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "$username",
                        "password": "$password"
                    }
                """.trimIndent())
        ).andDo(print())

        val member = memberRepository.findByUsername(username).orElseThrow()

        resultActions
            .andExpect(handler().handlerType(ApiV1MemberController::class.java))
            .andExpect(handler().methodName("login"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("${member.nickname}님 반갑습니다!"))
            .andExpect(jsonPath("$.data.apiKey").exists())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.memberDto.id").value(member.id))
            .andExpect(jsonPath("$.data.memberDto.createDate").exists())
            .andExpect(jsonPath("$.data.memberDto.modifyDate").exists())
            .andExpect(jsonPath("$.data.memberDto.name").value(member.nickname))

        resultActions.andExpect { result ->
            val apiKeyCookie = assertNotNull(result.response.getCookie("apiKey"))
            assertThat(apiKeyCookie.path).isEqualTo("/")
            assertThat(apiKeyCookie.domain).isEqualTo("localhost")
            assertThat(apiKeyCookie.isHttpOnly).isTrue()
            assertThat(apiKeyCookie.value).isNotBlank()

            val accessTokenCookie = assertNotNull(result.response.getCookie("accessToken"))
            assertThat(accessTokenCookie.path).isEqualTo("/")
            assertThat(accessTokenCookie.domain).isEqualTo("localhost")
            assertThat(accessTokenCookie.isHttpOnly).isTrue()
        }
    }

    @Test
    @DisplayName("로그아웃")
    fun t4() {
        val resultActions = mvc.perform(
            delete("/api/v1/members/logout")
        ).andDo(print())

        resultActions
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resultCode").value("200-1"))
            .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))
            .andExpect { result ->
                val apiKeyCookie = assertNotNull(result.response.getCookie("apiKey"))
                assertThat(apiKeyCookie.value).isEmpty()
                assertThat(apiKeyCookie.maxAge).isEqualTo(0)
                assertThat(apiKeyCookie.path).isEqualTo("/")
                assertThat(apiKeyCookie.isHttpOnly).isTrue()
            }
    }

    @Test
    @DisplayName("내 정보, 올바른 API KEY, 유효하지 않은 accessToken")
    fun t5() {
        val actor = memberRepository.findByUsername("user1").orElseThrow()
        val actorApiKey = assertNotNull(actor.apiKey)

        val resultActions = mvc.perform(
            get("/api/v1/members/me")
                .cookie(Cookie("apiKey", actorApiKey), Cookie("accessToken", "wrong-access-token"))
        ).andDo(print())

        resultActions
            .andExpect(status().isOk)
    }
}
