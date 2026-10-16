package com.back.p67260811.domain.member.service

import com.back.p67260811.domain.member.entity.Member
import com.back.p67260811.domain.member.repository.MemberRepository
import com.back.p67260811.global.exception.ServiceException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val authTokenService: AuthTokenService,
    private val passwordEncoder: PasswordEncoder
) {

    fun count(): Long {
        return memberRepository.count()
    }

    @JvmOverloads
    fun join(
        username: String,
        password: String?,
        nickname: String?,
        apiKey: String? = null
    ): Member {
        val apiKey = apiKey ?: UUID.randomUUID().toString()

        if (memberRepository.findByUsername(username).isPresent) {
            throw ServiceException("409-1", "이미 사용중인 아이디입니다.")
        }

        val member =
            Member(username, passwordEncoder.encode(password), nickname, apiKey)
        return memberRepository.save<Member>(member)
    }

    fun findByUsername(username: String): Member =
        findByUsernameOrNull(username) ?: throw ServiceException(
            "404-1",
            "존재하지 않는 아이디입니다."
        )

    fun findByUsernameOrNull(username: String): Member? =
        memberRepository.findByUsername(username).orElse(null)

    fun findByApiKey(apiKey: String): Member =
        findByApiKeyOrNull(apiKey) ?: throw ServiceException(
            "404-2",
            "존재하지 않는 API 키입니다."
        )

    fun findByApiKeyOrNull(apiKey: String): Member? =
        memberRepository.findByApiKey(apiKey).orElse(null)

    fun genAccessToken(member: Member): String =
        authTokenService.genAccessToken(member)

    fun payloadOrNull(jwt: String): Map<String, Any>? =
        authTokenService.payloadOrNull(jwt)

    fun findById(id: Int): Member = memberRepository.findById(id).orElseThrow {
            ServiceException("404-3", "존재하지 않는 회원입니다.")
        }

    fun findAll(): List<Member> = memberRepository.findAll()

    fun checkPassword(inputPassword: String?, rawPassword: String?) {
        if (!passwordEncoder.matches(
                inputPassword,
                rawPassword)) {
            throw ServiceException("401-2", "비밀번호가 일치하지 않습니다.")
        }
    }
}
