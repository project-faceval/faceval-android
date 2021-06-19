package com.chardon.faceval.android.rest.client

import com.chardon.faceval.android.util.DateFormatUtil
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private val retrofit by lazy {
    Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()
}

interface UserCheckClient {

    @GET("/check/{username}")
    fun checkValidity(@Path("username") userName: String): Deferred<String>
}

val APISet.userNameCheckClient: UserCheckClient by lazy {
    retrofit.create(UserCheckClient::class.java)
}