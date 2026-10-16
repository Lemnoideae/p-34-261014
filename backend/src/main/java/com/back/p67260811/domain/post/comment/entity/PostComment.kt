package com.back.p67260811.domain.post.comment.entity;

import com.back.p67260811.domain.post.post.entity.Post;
import com.back.p67260811.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class PostComment extends BaseEntity {
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public PostComment() {}

    public PostComment(String content, Post post) {
        this.content = content;
        this.post = post;
    }

    public void modify(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public Post getPost() {
        return post;
    }

}
