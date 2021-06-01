package com.chardon.faceval.android.rest.client

import com.chardon.faceval.android.rest.model.PostUser
import com.chardon.faceval.android.rest.model.UserInfo
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://127.0.0.1:8729/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface UserClient {

    @GET("user")
    fun getUser(@Query("id") userName: String): Call<UserInfo>

    @POST("user")
    fun createUser(@Body user: PostUser): Call<UserInfo>

    @PUT("user")
    fun updateUser(@Body user: PostUser): Call<UserInfo>

    @DELETE("user")
    fun deleteUser(@Query("id") userName: String,
                   @Query("password") password: String): Call<Map<String, String>>

    @PATCH("user")
    fun updatePassword(@Query("id") userName: String,
                       @Query("password") oldPassword: String,
                       @Query("new_password") newPassword: String): Call<Map<String, String>>

    @POST("auth")
    fun authenticate(@Query("id") userName: String,
                     @Query("password") password: String): Call<UserInfo>
}

interface PhotoClient {

    fun getPhoto()
}

object APISet {
    val userClient: UserClient by lazy {
        retrofit.create(UserClient::class.java)
    }

    val photoClient: PhotoClient by lazy {
        retrofit.create(PhotoClient::class.java)
    }
}