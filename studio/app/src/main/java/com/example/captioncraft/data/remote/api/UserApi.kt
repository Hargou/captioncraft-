package com.example.captioncraft.data.remote.api

import com.example.captioncraft.data.remote.dto.LoginDto
import com.example.captioncraft.data.remote.dto.LoginResponseDto
import com.example.captioncraft.data.remote.dto.RegisterDto
import com.example.captioncraft.data.remote.dto.RegisterResponseDto
import com.example.captioncraft.data.remote.dto.UserDto
import retrofit2.http.*

interface UserApi {

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserDto

    @POST("users")
    suspend fun createUser(@Body user: UserDto): UserDto

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: UserDto): UserDto

    @GET("users")
    suspend fun getAllUsers(): List<UserDto>

    @POST("register")
    suspend fun register(@Body request: RegisterDto): RegisterResponseDto

    @POST("login")
    suspend fun authenticate(@Body request: LoginDto): LoginResponseDto
}
