package com.chardon.faceval.android.rest.client

import com.chardon.faceval.android.util.DateFormatUtil
import com.chardon.faceval.entity.*
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://47.109.80.112:9988/"

private val retrofit by lazy {
    Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper().apply {
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
            dateFormat = DateFormatUtil.dateFormat
        }))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()
}

interface UserClient {

    @GET("/user")
    fun getUserAsync(@Query("username") userName: String): Deferred<UserInfo>

    @FormUrlEncoded
    @POST("/user")
    fun createUserAsync(@Body user: UserInfoUpload): Deferred<UserInfo>

    @PUT("/user")
    fun updateUserAsync(@Body user: UserInfoUpload): Deferred<UserInfo>

    @DELETE("/user")
    fun deleteUserAsync(@Query("username") userName: String,
                        @Query("password") password: String): Deferred<Map<String, String>>

    @PATCH("/user")
    fun updatePasswordAsync(@Query("username") userName: String,
                            @Query("password") oldPassword: String,
                            @Query("new_password") newPassword: String): Deferred<Map<String, String>>

    @FormUrlEncoded
    @POST("/login")
    fun loginAsync(@Field("username") userName: String,
                   @Field("password") password: String): Deferred<UserInfo>
}

interface PhotoClient {

    @GET("/photo")
    fun getPhotosAsync(@Path("photo_id") photoId: Long,
                       @Path("user_id") userName: String): Deferred<List<PhotoInfo>>

    @FormUrlEncoded
    @POST("/photo")
    fun addPhotoAsync(@Body newPhoto: PhotoInfoUploadBase64): Deferred<PhotoInfo>

    @PUT("/photo")
    fun updatePhotoInfoAsync(@Body newPhotoInfo: PhotoInfoUpdate): Deferred<PhotoInfo>

    @DELETE("/photo")
    fun deletePhotoAsync(@Query("id") userName: String,
                         @Query("password") password: String,
                         @Query("photo_id") photoId: Long): Deferred<Map<String, String>>
}

interface AIClient {

    @FormUrlEncoded
    @POST("/eval")
    fun scoreAsync(@Body scoring: ScoringModelBase64): Deferred<List<Double>>

    @FormUrlEncoded
    @POST("/eval/detect")
    fun detectAsync(@Body detection: DetectionModelBase64): Deferred<DetectionResult>

    @FormUrlEncoded
    @POST("/eval/scoring")
    fun scoreDetectedAsync(@Body scoring: ScoringModelBase64): Deferred<List<Double>>
}

object APISet {
    val userClient: UserClient by lazy {
        retrofit.create(UserClient::class.java)
    }

    val photoClient: PhotoClient by lazy {
        retrofit.create(PhotoClient::class.java)
    }

    val aiClient: AIClient by lazy {
        retrofit.create(AIClient::class.java)
    }
}