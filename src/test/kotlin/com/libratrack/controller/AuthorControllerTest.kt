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
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorControllerTest {
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
    fun `著者登録が成功すること`() {
        val request = mapOf(
            "name" to "テスト著者",
            "birthDate" to "1990-01-01"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/authors")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("テスト著者"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value("1990-01-01"))
    }

    @Test
    fun `名前が空の場合エラーになること`() {
        val request = mapOf(
            "name" to "",
            "birthDate" to "1990-01-01"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/authors")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("名前は必須です"))
    }

    @Test
    fun `生年月日が未来の日付の場合エラーになること`() {
        val request = mapOf(
            "name" to "テスト著者",
            "birthDate" to "2100-01-01"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/authors")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("生年月日は過去の日付である必要があります"))
    }

    @Test
    fun `著者更新が成功すること`() {
        // テスト用の著者を作成
        val authorId = testDataManager.createAuthor()
        
        val request = mapOf(
            "id" to authorId,
            "name" to "更新後の名前",
            "birthDate" to "1991-01-01"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/authors")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("更新後の名前"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value("1991-01-01"))
    }

    @Test
    fun `存在しない著者IDで更新しようとするとエラーになること`() {
        val nonExistentAuthorId = testDataManager.getNonExistentAuthorId()
        
        val request = mapOf(
            "id" to nonExistentAuthorId,
            "name" to "更新テスト",
            "birthDate" to "1990-01-01"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("/authors")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("AUTHOR_NOT_FOUND"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("著者ID $nonExistentAuthorId は存在しません"))
    }
} 