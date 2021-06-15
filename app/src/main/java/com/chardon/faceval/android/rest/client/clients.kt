package com.chardon.faceval.android.rest.client

import com.chardon.faceval.entity.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*

private const val BASE_URL = "http://127.0.0.1:9988/"

object ObjectMapperFactory {

    fun getObjectMapper(): ObjectMapper {
        val newObjectMapper = ObjectMapper()
        newObjectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

        return newObjectMapper
    }
}

private val retrofit = Retrofit.Builder()
//    .addConverterFactory(MoshiConverterFactory.create())
    .addConverterFactory(JacksonConverterFactory.create(ObjectMapperFactory.getObjectMapper()))
//    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface UserClient {

    @GET("/user")
    fun getUser(@Query("username") userName: String): Call<UserInfo>

    @FormUrlEncoded
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
    fun login(@Field("username") userName: String,
              @Field("password") password: String): Call<UserInfo>
}

interface PhotoClient {

    @GET("/photo/{photo_id}/{user_id}")
    fun getPhotos(@Path("photo_id") photoId: Long,
                  @Path("user_id") userName: String): Call<List<PhotoInfo>>

    @Multipart
    @FormUrlEncoded
    @POST("/photo")
    fun addPhoto(@Body newPhoto: PhotoInfoUpload<MultipartBody.Part>): Call<PhotoInfo>

    @PUT("/photo")
    fun updatePhotoInfo(@Body newPhotoInfo: PhotoInfoUpdate): Call<PhotoInfo>

    @DELETE("/photo")
    fun deletePhoto(@Query("id") userName: String,
                    @Query("password") password: String,
                    @Query("photo_id") photoId: Long): Call<Map<String, String>>
}

interface AIClient {

    @Multipart
    @FormUrlEncoded
    @POST("/eval")
    fun score(@Field("ext") extension: String,
              @Part("bimg") image: MultipartBody.Part): Call<List<Double>>

    @Multipart
    @FormUrlEncoded
    @POST("/eval/detect")
    fun detect(@Field("ext") extension: String,
               @Part("bimg") image: MultipartBody.Part): Call<DetectionResult>

    @Multipart
    @FormUrlEncoded
    @POST("/eval/scoring")
    fun scoreDetected(@Field("ext") extension: String,
                      @Part("bimg") image: MultipartBody.Part,
                      @Body detectionResult: DetectionResult): Call<List<Double>>
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