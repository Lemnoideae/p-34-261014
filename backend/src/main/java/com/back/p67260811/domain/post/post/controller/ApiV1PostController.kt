package com.back.p67260811.domain.post.post.controller

import com.back.p67260811.domain.post.post.dto.PostDto
import com.back.p67260811.domain.post.post.service.PostService
import com.back.p67260811.global.dto.RsData
import com.back.p67260811.global.rq.Rq
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts")
@SecurityRequirement(name = "bearerAuth")
class ApiV1PostController(
    private val postService: PostService,
    private val rq: Rq
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): List<PostDto> = postService.findAll().map(::PostDto)

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int): PostDto = PostDto(postService.findById(id))

    data class PostWriteReqBody(
        @field:Size(min = 2, max = 10, message = "제목은 2글자 이상 10글자 이하로 작성해주세요.")
        @field:NotBlank(message = "제목을 입력해주세요.")
        val title: String,

        @field:Size(min = 2, max = 10, message = "내용은 2글자 이상 10글자 이하로 작성해주세요.")
        @field:NotBlank(message = "내용을 입력해주세요.")
        val content: String
    )

    @PostMapping
    @Transactional
    fun write(@Valid @RequestBody reqBody: PostWriteReqBody): RsData<PostDto> {
        val post = postService.write(rq.actor, reqBody.title, reqBody.content)
        return RsData(
            "201-1",
            "${post.id}번 글이 성공적으로 등록되었습니다",
            PostDto(post)
        )
    }

    data class PostModifyReqBody(
        @field:Size(min = 2, max = 10, message = "제목은 2글자 이상 10글자 이하로 작성해주세요.")
        @field:NotBlank(message = "제목을 입력해주세요.")
        val title: String,

        @field:Size(min = 2, max = 10, message = "내용은 2글자 이상 10글자 이하로 작성해주세요.")
        @field:NotBlank(message = "내용을 입력해주세요.")
        val content: String
    )

    @PatchMapping("/{id}")
    @Transactional
    fun modify(@PathVariable id: Int, @Valid @RequestBody reqBody: PostModifyReqBody): RsData<Void> {
        val post = postService.findById(id)
        post.checkActorModify(rq.actor)
        postService.modify(post, reqBody.title, reqBody.content)
        return RsData(
            "200-1",
            "${id}번 게시물이 수정되었습니다."
        )
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int): RsData<Void> {
        val post = postService.findById(id)
        post.checkActorDelete(rq.actor)
        postService.delete(id)
        return RsData(
            "200-1",
            "${id}번 게시물이 삭제되었습니다."
        )
    }
}
