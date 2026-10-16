package com.back.p67260811.domain.post.post.controller

import com.back.p67260811.domain.member.entity.Member
import com.back.p67260811.domain.member.repository.MemberRepository
import com.back.p67260811.domain.post.post.entity.Post
import com.back.p67260811.domain.post.post.repository.PostRepository
import com.back.p67260811.standard.Ut
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.transaction.annotation.Transactional

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsInRelativeOrder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Value($$"${custom.jwt.secret-key}")
    private lateinit var secretPattern: String

    @Value($$"${custom.jwt.expireMills}")
    private var expireSeconds: Long = 0L

    @Test
    @DisplayName("글 다건 조회")
    fun t1() {
	val resultActions = mvc
		.perform(
			get("/api/v1/posts")
		)
		.andDo(print())

	resultActions
		.andExpect(status().isOk())

	resultActions
		.andExpect(jsonPath("$.length()").value(3))
		.andExpect(jsonPath("$[*].id", containsInRelativeOrder(3, 1)))
		.andExpect(jsonPath("$[0].id").value(3))
		.andExpect(jsonPath("$[0].createDate").exists())
		.andExpect(jsonPath("$[0].modifyDate").exists())
		.andExpect(jsonPath("$[0].title").value("제목3"))
		.andExpect(jsonPath("$[0].content").value("내용3"))
		.andExpect(jsonPath("$[0].nickname").value("유저2"))
		.andExpect(jsonPath("$[0].username").value("user2"))
    }

    @Test
    @DisplayName("글 작성")
    fun t2() {
	val title = "제목입니다"
	val content = "내용입니다"

	val resultActions = mvc
		.perform(
			post("/api/v1/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer user1")
				.content("""
					{
					    "title": "%s",
					    "content": "%s"
					}
					""".format(title, content))
		)
		.andDo(print())

	resultActions
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$.resultCode").value("201-1"))
		.andExpect(jsonPath("$.msg").value("4번 글이 성공적으로 등록되었습니다"))
		.andExpect(jsonPath("$.data.id").value(4))
		.andExpect(jsonPath("$.data.createDate").exists())
		.andExpect(jsonPath("$.data.modifyDate").exists())
		.andExpect(jsonPath("$.data.title").value(title))
		.andExpect(jsonPath("$.data.content").value(content))
		.andExpect(jsonPath("$.data.nickname").value("유저1"))
		.andExpect(jsonPath("$.data.username").value("user1"))
    }

    @Test
    @DisplayName("글 수정")
    fun t3() {
	val targetId = 1
	val title = "제목 수정"
	val content = "내용 수정"

	val resultActions = mvc
		.perform(
			patch("/api/v1/posts/$targetId")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer user1")
				.content("""
					{
					    "title": "%s",
					    "content": "%s"
					}
					""".format(title, content))
		)
		.andDo(print())

	resultActions
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.resultCode").value("200-1"))
		.andExpect(jsonPath("$.msg").value("${ targetId }번 게시물이 수정되었습니다."))

	val post = postRepository.findById(targetId).get()
	assertThat(post.title).isEqualTo(title)
	assertThat(post.content).isEqualTo(content)
    }

    @Test
    @DisplayName("글 단건 조회")
    fun t4() {
	val targetId = 1

	val resultActions = mvc
		.perform(
			get("/api/v1/posts/$targetId")
		)
		.andDo(print())

	resultActions
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.id").value(1))
		.andExpect(jsonPath("$.createDate").exists())
		.andExpect(jsonPath("$.modifyDate").exists())
		.andExpect(jsonPath("$.title").value("제목1"))
		.andExpect(jsonPath("$.content").value("내용1"))
		.andExpect(jsonPath("$.nickname").value("유저1"))
		.andExpect(jsonPath("$.username").value("user1"))
    }

    @Test
    @DisplayName("글 삭제")
    fun t5() {
	val targetId = 1

	val resultActions = mvc
		.perform(
			delete("/api/v1/posts/${targetId}")
				.header("Authorization", "Bearer user1")
		)
		.andDo(print())

	resultActions
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.resultCode").value("200-1"))
		.andExpect(jsonPath("$.msg").value("${targetId}번 게시물이 삭제되었습니다."))

	val post = postRepository.findById(targetId).orElse(null)
	assertThat(post).isNull()
    }

    @Test
    @DisplayName("다른 회원은 글을 수정하거나 삭제할 수 없다.")
    fun t10() {
	val targetId = 1

	mvc.perform(
			patch("/api/v1/posts/$targetId")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer user2")
				.content("""
					{
					    "title": "수정 제목",
					    "content": "수정 내용"
					}
					""")
		)
		.andExpect(status().isForbidden())
		.andExpect(jsonPath("$.resultCode").value("403-1"))

	mvc.perform(
			delete("/api/v1/posts/$targetId")
				.header("Authorization", "Bearer user2")
		)
		.andExpect(status().isForbidden())
		.andExpect(jsonPath("$.resultCode").value("403-2"))

	val post = postRepository.findById(targetId).orElseThrow()
	assertThat(post.title).isEqualTo("제목1")
    }

    @Test
    @DisplayName("로그인하지 않아도 댓글 목록과 단건을 조회할 수 있다.")
    fun t11() {
	mvc.perform(get("/api/v1/posts/1/comments"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$[0].content").value("댓글 1-3"))

	mvc.perform(get("/api/v1/posts/1/comments/1"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content").value("댓글 1-1"))
    }

    @Test
    @DisplayName("글 작성, 제목이 입력되지 않은 경우")
    fun t6() {
	val title = ""
	val content = "내용입니다"

	val resultActions = mvc
		.perform(
			post("/api/v1/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer user1")
				.content("""
					{
					    "title": "$title",
					    "content": "$content"
					}
					""".trimIndent())
		)
		.andDo(print())

	resultActions
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.resultCode").value("400-1"))
		.andExpect(jsonPath("$.msg").value("title-NotBlank-제목을 입력해주세요.\ntitle-Size-제목은 2글자 이상 10글자 이하로 작성해주세요."))
    }

    @Test
    @DisplayName("글 작성, 내용이 입력되지 않은 경우")
    fun t7() {
	val title = "제목입니다."
	val content = ""

	val resultActions = mvc
		.perform(
			post("/api/v1/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer user1")
				.content("""
					{
					    "title": "$title",
					    "content": "$content"
					}
					""".trimIndent())
		)
		.andDo(print())

	resultActions
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.resultCode").value("400-1"))
		.andExpect(jsonPath("$.msg").value("content-NotBlank-내용을 입력해주세요.\ncontent-Size-내용은 2글자 이상 10글자 이하로 작성해주세요."))
    }

    @Test
    @DisplayName("글 작성, JSON 양식이 잘못된 경우")
    fun t8() {
	val title = "제목입니다."
	val content = "내용입니다"

	val resultActions = mvc
		.perform(
			post("/api/v1/posts")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer user1")
				.content("""
					{
					    "title": "$title",
					    "content": "$content"

					""".trimIndent())
		)
		.andDo(print())

	resultActions
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.resultCode").value("400-2"))
		.andExpect(jsonPath("$.msg").value("잘못된 형식의 요청 데이터입니다."))
    }

    @Test
    @DisplayName("글 작성, 유효한 엑세스 토큰, 잘못된 apiKey")
    fun t9() {
	val title = "제목입니다"
	val content = "내용입니다"
	val author = memberRepository.findByUsername("user1").get()

	val accessToken = Ut.jwt.toString(
		secretPattern,
		expireSeconds,
		mapOf("id" to author.id, "username" to author.username, "nickname" to author.nickname)
	)

	val resultActions = mvc
		.perform(
			post("/api/v1/posts")
				.header("Authorization", "Bearer apsdifjaiofjoaei %s".format(accessToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					    "title": "$title",
					    "content": "$content"
					}
					""".trimIndent())
		)
		.andDo(print())

	resultActions
		.andExpect(status().isCreated())
    }
}