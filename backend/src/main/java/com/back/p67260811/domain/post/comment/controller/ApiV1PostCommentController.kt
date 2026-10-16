package com.back.p67260811.domain.post.comment.controller

import com.back.p67260811.domain.post.comment.dto.PostCommentDto
import com.back.p67260811.domain.post.comment.entity.PostComment
import com.back.p67260811.domain.post.post.service.PostService
import com.back.p67260811.global.dto.RsData
import com.back.p67260811.global.rq.Rq
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
class ApiV1PostCommentController(
    private val postService: PostService,
    private val rq: Rq
) {

    @GetMapping
    fun list(
        @PathVariable postId: Int
    ): List<PostCommentDto> = postService.findById(postId)
        .getComments().reversed().map { PostCommentDto(it) }

    @GetMapping("/{commentId}")
    fun item(
        @PathVariable postId: Int,
        @PathVariable commentId: Int
    ): PostCommentDto = postService.findById(postId)
        .findCommentById(commentId).let { PostCommentDto(it) }


    @JvmRecord
    data class CommentWriteForm(
        val content: @NotBlank(message = "댓글 내용을 입력해주세요.") @Size(
            min = 2,
            max = 100,
            message = "댓글 내용은 2글자 이상 100글자 이하로 입력해주세요."
        ) String
    )

    @PostMapping
    @Transactional
    fun write(
        @PathVariable postId: Int,
        @Valid form: @Valid CommentWriteForm
    ): RsData<PostCommentDto> {

        val post = postService.findById(postId)
        val postComment = postService.writeComment(post, form.content)

        postService.flush()
        return RsData(
            "201-1",
            "${postComment.id}번 댓글이 성공적으로 등록되었습니다.",
            PostCommentDto(postComment)
        )
    }

    @DeleteMapping("/{commentId}")
    @Transactional
    fun delete(
        @PathVariable postId: Int,
        @PathVariable commentId: Int
    ): RsData<Void> {
        val post = postService.findById(postId)
        postService.deleteComment(post, commentId)

        return RsData(
            "200-1",
            "${commentId}번 댓글이 삭제되었습니다."
        )
    }

    @JvmRecord
    data class CommentModifyForm(
        val content: @NotBlank(message = "댓글 내용을 입력해주세요.") @Size(
            min = 2,
            max = 100,
            message = "댓글 내용은 2글자 이상 100글자 이하로 입력해주세요."
        ) String?
    )

    @PatchMapping("/{commentId}")
    @Transactional
    fun modify(
        @PathVariable postId: Int,
        @PathVariable commentId: Int,
        @Valid form: @Valid CommentModifyForm
    ): RsData<Void> {
        val post = postService.findById(postId)
        postService.modifyComment(post, commentId, form.content!!)

        return RsData(
            "200-1",
            "${commentId}번 댓글이 수정되었습니다."
        )
    }
}