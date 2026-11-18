package com.example.apptiket.data

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiResponse<LoginData>>

    @GET("auth/me")
    suspend fun me(): Response<ApiResponse<UserDto>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
}

interface TicketsApi {
    @GET("tickets")
    suspend fun getTickets(): Response<ApiResponse<PaginatedData<TicketDto>>>

    @POST("tickets/{id}/tomar")
    suspend fun tomarTicket(
        @Path("id") id: Int,
        @Body body: TomarTicketRequest = TomarTicketRequest()
    ): Response<ApiResponse<TicketDto>>

    @Multipart
    @POST("tickets/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Int,
        @Part("_method") method: RequestBody,
        @Part("estado") estado: RequestBody,
        @Part("comentario") comentario: RequestBody?,
        @Part evidencia: MultipartBody.Part
    ): Response<ApiResponse<CambiarEstadoData>>
}

object ApiClient {

    // Cambia esta IP por la IPv4 de tu computadora (ej. http://192.168.1.50:8000/api/)
    private const val BASE_URL = "http://192.168.100.7:8000/api/"

    private fun buildRetrofit(session: SessionManager): Retrofit {
        val authInterceptor = Interceptor { chain ->
            val builder = chain.request().newBuilder()
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
    }

    fun create(session: SessionManager): AuthApi =
        buildRetrofit(session).create(AuthApi::class.java)

    fun createTickets(session: SessionManager): TicketsApi =
        buildRetrofit(session).create(TicketsApi::class.java)
}