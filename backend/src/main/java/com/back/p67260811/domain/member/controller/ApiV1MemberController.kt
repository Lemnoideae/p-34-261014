package com.back.p67260811.domain.member.controller

import com.back.p67260811.domain.member.dto.MemberDto
import com.back.p67260811.domain.member.service.MemberService
import com.back.p67260811.global.dto.RsData
import com.back.p67260811.global.exception.ServiceException
import com.back.p67260811.global.rq.Rq
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
@SecurityRequirement(name = "bearerAuth")
class ApiV1MemberController(
    private val memberService: MemberService,
    private val rq: Rq
) {

    @JvmRecord
    data class JoinReqBody(
        val username: @NotBlank @Size(min = 2, max = 30) String,
        val password: @NotBlank @Size(min = 2, max = 30) String,
        val nickname: @NotBlank @Size(min = 2, max = 30) String
    )

    @JvmRecord
    data class JoinResBody(
        val memberDto: MemberDto
    )

    @PostMapping("/join")
    fun join(
        @RequestBody @Valid reqBody: @Valid JoinReqBody
    ): RsData<JoinResBody> {
        val member = memberService.join(
            reqBody.username,
            reqBody.password,
            reqBody.nickname)

        return RsData(
            "201-1",
            "회원가입이 완료되었습니다. ${reqBody.nickname}님 환영합니다.",
            JoinResBody(MemberDto(member))
        )
    }

    @JvmRecord
    data class LoginReqBody(
        val username: @NotBlank @Size(min = 2, max = 30) String,
        val password: @NotBlank @Size(min = 2, max = 30) String
    )

    @JvmRecord
    data class LoginResBody(
        val memberDto: MemberDto,
        val apiKey: String,
        val accessToken: String
    )

    @PostMapping("/login")
    fun login(
        @RequestBody @Valid reqBody: @Valid LoginReqBody
    ): RsData<LoginResBody> {

        val actor = memberService.findByUsernameOrNull(reqBody.username)
            ?: throw ServiceException("401-1", "존재하지 않는 회원입니다.")

        memberService.checkPassword(
            reqBody.password,
            actor.password)

        val accessToken = memberService.genAccessToken(actor)
        rq.apply {
            actor.apiKey?.let { addCookie("apiKey", it) }
            addCookie("accessToken", accessToken)
        }

        return RsData(
            "200-1",
            "${actor.nickname}님 반갑습니다!",
            actor.apiKey?.let {
                LoginResBody(
                    MemberDto(actor),
                    it,
                    accessToken
                )
            }
        )
    }

    @DeleteMapping("/logout")
    fun logout(): RsData<Void> {
        rq.deleteCookie("apiKey")

        return RsData(
            "200-1",
            "로그아웃 되었습니다."
        )
    }

    @JvmRecord
    data class MeResBody(
        val member: MemberDto
    )

    @GetMapping("/me")
    fun me(): RsData<MeResBody> {
        val actor = rq.actor // 인증
        val realActor =
            actor.id?.let { memberService.findById(it) }

        return RsData(
            "200-1",
            "OK",
            realActor?.let { MeResBody(MemberDto(it)) }
        )
    }
}
