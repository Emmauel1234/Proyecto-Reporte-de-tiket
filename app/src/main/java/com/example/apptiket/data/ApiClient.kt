package com.example.apptiket.data


import com.example.apptiket.data.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<LoginData>>

    @GET("auth/me")
    suspend fun me(): Response<ApiResponse<UserDto>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
}

object ApiClient {

    // Emulador -> tu PC. En celular físico usa la IP de tu PC (ej. http://192.168.1.50:8000/api/)
    // IMPORTANTE: debe terminar con "/"
    private const val BASE_URL = "http://10.0.2.2:8000/api/"

    fun create(session: SessionManager): AuthApi {
        val authInterceptor = Interceptor { chain ->
            val builder = chain.request().newBuilder()
                // Sin este header, Laravel responde con HTML/redirecciones en los errores
                .header("Accept", "application/json")

            session.token?.let { builder.header("Authorization", "Bearer $it") }
            chain.proceed(builder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}