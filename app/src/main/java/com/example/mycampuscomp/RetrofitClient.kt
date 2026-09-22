package com.example.mycampuscomp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://mycampuscomp-api2026-bhcha3cwgtdmckbg.brazilsouth-01.azurewebsites.net/"

    private const val GEMINI_BASE_URL =
        "https://generativelanguage.googleapis.com/"

    val aiService: AIService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(AIService::class.java)
    }

    val geminiService: GeminiApiService by lazy {

        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(GeminiApiService::class.java)
    }
}