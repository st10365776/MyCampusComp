package com.example.mycampuscomp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://mycampuscomp-api2026-bhcha3cwgtdmckbg.brazilsouth-01.azurewebsites.net"

    val aiService: AIService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AIService::class.java)
    }
}


