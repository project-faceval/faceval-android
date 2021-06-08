package com.chardon.faceval.android.rest.client

import android.media.Image
import com.chardon.faceval.entity.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://127.0.0.1:9988/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface UserClient {

    @GET("/user")
    fun getUser(@Query("username") userName: String): Call<UserInfo>

    @POST("/user")
    fun createUser(@Body user: UserInfoUpload): Call<UserInfo>

    @PUT("/user")
    fun updateUser(@Body user: UserInfoUpload): Call<UserInfo>

    @DELETE("/user")
    fun deleteUser(@Query("username") userName: String,
                   @Query("password") password: String): Call<Map<String, String>>

    @PATCH("/user")
    fun updatePassword(@Query("username") userName: String,
                       @Query("password") oldPassword: String,
                       @Query("new_password") newPassword: String): Call<Map<String, String>>

    @POST("/login")
    fun login(@Query("username") userName: String,
              @Query("password") password: String): Call<UserInfo>
}

interface PhotoClient {

    @GET("/photo/{photo_id}/{user_id}")
    fun getPhoto(@Path("photo_id") photoId: Long,
                 @Path("user_id") userName: String): Call<PhotoInfo>

    @POST("/photo")
    fun addPhoto(@Body newPhoto: PhotoInfoUpload<Image>): Call<PhotoInfo>

    @PUT("/photo")
    fun updatePhotoInfo(@Body newPhotoInfo: PhotoInfoUpdate): Call<PhotoInfo>

    @DELETE("/photo")
    fun deletePhoto(@Query("id") userName: String,
                    @Query("password") password: String,
                    @Query("photo_id") photoId: Long): Call<Map<String, String>>
}

interface AIClient {

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