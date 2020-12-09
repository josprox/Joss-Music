package com.zionhuang.music.youtube

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {
    private const val GoogleSuggestionAPIBaseUrl = "https://clients1.google.com/"

    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(GoogleSuggestionAPIBaseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder()
                    .registerTypeAdapter(SuggestionResult::class.java, SuggestionResult.deserializer)
                    .create()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

    val suggestionAPIService: SuggestionAPIService = retrofit.create(SuggestionAPIService::class.java)
}