package com.back.p67260811.domain.post.post.entity

import com.back.p67260811.domain.member.entity.Member
import com.back.p67260811.domain.post.comment.entity.PostComment
import com.back.p67260811.global.exception.ServiceException
import com.back.p67260811.global.jpa.entity.BaseEntity
import jakarta.persistence.*

@Entity
class Post(
    @field:ManyToOne(fetch = FetchType.LAZY)
    var author: Member,
    var title: String,
    var content: String

) : BaseEntity() {

    @OneToMany(
        mappedBy = "post",
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        orphanRemoval = true
    )
    private val comments: MutableList<PostComment> = mutableListOf()

    // TODO: Optional 제거 및 stream 제거
    fun findCommentById(id: Int): PostComment {
        return comments
            .stream()
            .filter { c: PostComment -> c.id == id }
            .findFirst()
            .orElseThrow { IllegalArgumentException("해당 댓글이 존재하지 않습니다") }
    }

    fun addComment(content: String): PostComment {
        val postComment = PostComment(content, this).also {
            comments.add(it)
        }

        return postComment
    }

    fun removeComment(id: Int) {
        comments.removeIf { comment: PostComment -> comment.id == id }
    }

    fun modifyComment(id: Int, content: String): PostComment =
        findCommentById(id).apply { modify(content) }

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }

    fun checkActorModify(actor: Member) {
        if (author.id == null || author.id != actor.id) {
            throw ServiceException("403-1", "수정 권한이 없습니다.")
        }
    }

    fun checkActorDelete(actor: Member) {
        if (author.id == null || author.id != actor.id) {
            throw ServiceException("403-2", "삭제 권한이 없습니다.")
        }
    }

    fun getComments(): List<PostComment> = comments.toList()
}
