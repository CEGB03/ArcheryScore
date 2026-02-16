package com.cegb03.archeryscore.data.remote

import com.cegb03.archeryscore.data.model.User
import com.cegb03.archeryscore.data.model.UpdateUserRequest
import com.cegb03.archeryscore.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Rutas públicas
    @POST("login")
    suspend fun loginUser(@Body credentials: Map<String, String>): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body user: User): User

    @GET("/")
    suspend fun checkApiStatus(): String

    // Rutas de usuarios
    @GET("users")
    suspend fun getAllUsers(): List<User>

    @GET("users/by-tel")
    suspend fun getUserByTel(@Query("tel") tel: String): User

    @GET("users/by-mail")
    suspend fun getUserByMail(@Query("email") email: String): User

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int?): User

    @POST("users/newUser")
    suspend fun newUser(@Body user: User): Response<Unit>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): Response<User>

    @POST("users/register")
    suspend fun registerUser(@Body user: User)

    @PUT("users/newPass/{id}")
    suspend fun updatedPassword(
        @Path("id") id: String,
        @Body request: PasswordChangeRequest
    ): Response<Void>

}