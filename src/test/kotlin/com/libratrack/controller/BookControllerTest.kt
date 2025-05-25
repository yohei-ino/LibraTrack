package com.libratrack.controller

import com.libratrack.util.TestDataManager
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var dslContext: DSLContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var testDataManager: TestDataManager

    @BeforeEach
    fun setup() {
        testDataManager = TestDataManager(dslContext)
        testDataManager.cleanupAllTables()
    }

    @AfterEach
    fun cleanup() {
        testDataManager.cleanupAllTables()
    }

    @Test
    fun `書籍登録が成功すること`() {
        // テスト用の著者を作成
        val authorId = testDataManager.createAuthor()
        
        val request = mapOf(
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("テスト本"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(1000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("unpublished"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.authors[0].id").value(authorId))
    }

    @Test
    fun `存在しない著者IDで書籍登録しようとするとエラーになること`() {
        val nonExistentAuthorId = testDataManager.getNonExistentAuthorId()
        
        val request = mapOf(
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(nonExistentAuthorId)
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("AUTHOR_NOT_FOUND"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("著者ID $nonExistentAuthorId は存在しません"))
    }

    @Test
    fun `タイトルが空の場合エラーになること`() {
        val request = mapOf(
            "title" to "",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(testDataManager.createAuthor())
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("タイトルは必須です"))
    }

    @Test
    fun `価格が負の数の場合エラーになること`() {
        val request = mapOf(
            "title" to "テスト本",
            "price" to -1000,
            "status" to "unpublished",
            "authorIds" to listOf(testDataManager.createAuthor())
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("価格は0以上である必要があります"))
    }

    @Test
    fun `書籍更新が成功すること`() {
        // テスト用の著者を作成
        val authorId = testDataManager.createAuthor()
        
        // テスト用の書籍を作成
        val bookId = testDataManager.createBook(authorIds = listOf(authorId))
        
        val request = mapOf(
            "id" to bookId,
            "title" to "更新後のタイトル",
            "price" to 2000,
            "status" to "published",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("更新後のタイトル"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(2000.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("published"))
    }

    @Test
    fun `存在しない書籍IDで更新しようとするとエラーになること`() {
        val nonExistentBookId = testDataManager.getNonExistentBookId()
        val authorId = testDataManager.createAuthor()
        
        val request = mapOf(
            "id" to nonExistentBookId,
            "title" to "更新テスト",
            "price" to 2000,
            "status" to "published",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("BOOK_NOT_FOUND"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("書籍ID $nonExistentBookId は存在しません"))
    }

    @Test
    fun `出版済みの書籍を未出版に変更しようとするとエラーになること`() {
        // テスト用の著者を作成
        val authorId = testDataManager.createAuthor()
        
        // テスト用の書籍を作成（published状態）
        val bookId = testDataManager.createBook(
            status = "published",
            authorIds = listOf(authorId)
        )
        
        val request = mapOf(
            "id" to bookId,
            "title" to "更新テスト",
            "price" to 2000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("INVALID_STATUS"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("出版済みの書籍を未出版に変更することはできません"))
    }

    @Test
    fun `著者の書籍一覧取得が成功すること`() {
        // テスト用の著者を作成
        val authorId = testDataManager.createAuthor()
        
        // 書籍を登録
        val request = mapOf(
            "title" to "テスト本",
            "price" to 1000,
            "status" to "unpublished",
            "authorIds" to listOf(authorId)
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/books")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)

        // 著者の書籍一覧を取得
        mockMvc.perform(MockMvcRequestBuilders.get("/authors/$authorId/books"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("テスト本"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].authors[0].id").value(authorId))
    }

    @Test
    fun `存在しない著者IDで書籍一覧取得しようとするとエラーになること`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/authors/999/books"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("AUTHOR_NOT_FOUND"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("著者ID 999 は存在しません"))
    }
} 