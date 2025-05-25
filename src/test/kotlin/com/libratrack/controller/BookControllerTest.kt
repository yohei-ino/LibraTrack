package com.libratrack.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class BookControllerTest : ApiTestBase() {

    private var authorId: Int = 0

    @BeforeEach
    fun setup() {
        // テスト用の著者を登録
        val request = mapOf(
            "name" to "テスト著者",
            "birthDate" to "1990-01-01"
        )

        val response = mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isOk)
            .andReturn()

        authorId = objectMapper.readTree(response.response.contentAsString).get("id").asInt()
    }

    @Test
    fun `書籍登録が成功すること`() {
        val request = mapOf(
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("テスト本"))
            .andExpect(jsonPath("$.price").value(1000))
            .andExpect(jsonPath("$.status").value("unpublished"))
            .andExpect(jsonPath("$.authors[0].id").value(authorId))
    }

    @Test
    fun `存在しない著者IDで書籍登録しようとするとエラーになること`() {
        val request = mapOf(
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(999)
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("AUTHOR_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("著者ID 999 は存在しません"))
    }

    @Test
    fun `タイトルが空の場合エラーになること`() {
        val request = mapOf(
            "title" to "",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("タイトルは必須です"))
    }

    @Test
    fun `価格が負の数の場合エラーになること`() {
        val request = mapOf(
            "title" to "テスト本",
            "price" to -1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("価格は0以上である必要があります"))
    }

    @Test
    fun `書籍更新が成功すること`() {
        // まず書籍を登録
        val createRequest = mapOf(
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        val response = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val bookId = objectMapper.readTree(response.response.contentAsString).get("id").asInt()

        // 書籍情報を更新
        val updateRequest = mapOf(
            "id" to bookId,
            "title" to "更新テスト本",
            "price" to 2000,
            "status" to "published",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            put("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("更新テスト本"))
            .andExpect(jsonPath("$.price").value(2000))
            .andExpect(jsonPath("$.status").value("published"))
    }

    @Test
    fun `存在しない書籍IDで更新しようとするとエラーになること`() {
        val request = mapOf(
            "id" to 999,
            "title" to "更新テスト本",
            "price" to 2000,
            "status" to "published",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            put("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("書籍ID 999 は存在しません"))
    }

    @Test
    fun `出版済みの書籍を未出版に変更しようとするとエラーになること`() {
        // まず書籍を登録して出版状態にする
        val createRequest = mapOf(
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        val response = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val bookId = objectMapper.readTree(response.response.contentAsString).get("id").asInt()

        // 出版状態に更新
        val publishRequest = mapOf(
            "id" to bookId,
            "title" to "テスト本",
            "price" to 1000,
            "status" to "published",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            put("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(publishRequest))
        )
            .andExpect(status().isOk)

        // 未出版に戻そうとする
        val unpublishRequest = mapOf(
            "id" to bookId,
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            put("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(unpublishRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_STATUS"))
            .andExpect(jsonPath("$.message").value("出版済みの書籍を未出版に変更することはできません"))
    }

    @Test
    fun `著者の書籍一覧取得が成功すること`() {
        // まず書籍を登録
        val request = mapOf(
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isOk)

        // 著者の書籍一覧を取得
        mockMvc.perform(get("/authors/$authorId/books"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].title").value("テスト本"))
            .andExpect(jsonPath("$[0].authors[0].id").value(authorId))
    }

    @Test
    fun `存在しない著者IDで書籍一覧取得しようとするとエラーになること`() {
        mockMvc.perform(get("/authors/999/books"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("AUTHOR_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("著者ID 999 は存在しません"))
    }
} 