package com.example.safesphere.SOSLocationSharing.data.API
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {
    private const val BASE_URL =
        "http://10.51.138.169:3000/"
    val api: SafeSphereApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(SafeSphereApi::class.java)
    }
}