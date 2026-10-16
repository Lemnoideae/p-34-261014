package com.back.p67260811.domain.member.controller

import com.back.p67260811.domain.member.dto.MemberWithUsernameDto
import com.back.p67260811.domain.member.service.MemberService
import com.back.p67260811.global.rq.Rq
import io.swagger.v3.oas.annotations.Operation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/adm/members")
class ApiV1AdmMemberController(
    private val memberService: MemberService,
    private val rq: Rq
) {

    @get:Operation(summary = "회원 다건 조회")
    @get:Transactional(readOnly = true)
    @get:GetMapping
    val items: List<MemberWithUsernameDto>
        get() = memberService.findAll()
            .map { MemberWithUsernameDto(it) }
}
