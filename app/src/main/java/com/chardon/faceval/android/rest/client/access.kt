//package com.chardon.faceval.android.rest.client
//
//import com.chardon.faceval.android.rest.model.ServerInfo
//import retrofit2.Call
//import retrofit2.Retrofit
//import retrofit2.converter.moshi.MoshiConverterFactory
//import retrofit2.http.GET
//import retrofit2.http.Headers
//
//private const val BASE_URL = "http://127.0.0.1:9237/eureka"
//
//@Deprecated("Use high-level web client")
//private val retrofit = Retrofit.Builder()
//    .addConverterFactory(MoshiConverterFactory.create())
//    .baseUrl(BASE_URL)
//    .build()
//
//@Deprecated("Use high-level web client")
//interface EurekaAccessClient {
//
//    @Headers("Accept: application/json")
//    @GET("apps")
//    fun getApps(): Call<ServerInfo>
//}
//
//@Deprecated("Use high-level web client")
//object EurekaAccessAbility {
//    val client: EurekaAccessClient by lazy {
//        retrofit.create(EurekaAccessClient::class.java)
//    }
//}