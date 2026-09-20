package com.example.mycampuscomp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AIService {

    @POST("api/AI/ask")
    suspend fun askAI(
        @Body request: AIRequest
    ): Response<AIResponse>
}