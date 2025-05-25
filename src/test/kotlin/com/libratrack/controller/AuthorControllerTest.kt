package com.libratrack.controller

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

class AuthorControllerTest : ApiTestBase() {

    @Test
    fun `著者登録が成功すること`() {
        val request = mapOf(
            "name" to "テスト著者",
            "birthDate" to "1990-01-01"
        )

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("テスト著者"))
            .andExpect(jsonPath("$.birthDate").value("1990-01-01"))
    }

    @Test
    fun `名前が空の場合エラーになること`() {
        val request = mapOf(
            "name" to "",
            "birthDate" to "1990-01-01"
        )

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("名前は必須です"))
    }

    @Test
    fun `生年月日が未来の日付の場合エラーになること`() {
        val futureDate = LocalDate.now().plusDays(1).toString()
        val request = mapOf(
            "name" to "テスト著者",
            "birthDate" to futureDate
        )

        mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("生年月日は過去の日付である必要があります"))
    }

    @Test
    fun `著者情報の更新が成功すること`() {
        // まず著者を登録
        val createRequest = mapOf(
            "name" to "テスト著者",
            "birthDate" to "1990-01-01"
        )

        val response = mockMvc.perform(
            post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(createRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val authorId = objectMapper.readTree(response.response.contentAsString).get("id").asInt()

        // 著者情報を更新
        val updateRequest = mapOf(
            "id" to authorId,
            "name" to "更新テスト著者",
            "birthDate" to "1990-01-01"
        )

        mockMvc.perform(
            put("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("更新テスト著者"))
    }

    @Test
    fun `存在しない著者IDで更新しようとするとエラーになること`() {
        val request = mapOf(
            "id" to 999,
            "name" to "更新テスト著者",
            "birthDate" to "1990-01-01"
        )

        mockMvc.perform(
            put("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("AUTHOR_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("著者ID 999 は存在しません"))
    }
} 