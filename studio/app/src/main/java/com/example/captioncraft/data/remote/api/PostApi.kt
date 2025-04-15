package com.example.captioncraft.data.remote.api

import com.example.captioncraft.data.remote.dto.PostDto
import com.example.captioncraft.data.remote.dto.PostResponse
import com.example.captioncraft.data.remote.dto.LikeRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface PostApi {
    @GET("post/{id}")
    suspend fun getPostById(@Path("id") id: Int): PostResponse

    @GET("post")
    suspend fun getAllPosts(): PostResponse

    @GET("post")
    suspend fun getPostsByUser(@Query("userId") userId: Int): PostResponse

    @Multipart
    @POST("post/create")
    suspend fun createPost(
        @Part("userId") userId: RequestBody,
        @Part("password") password: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("caption") caption: RequestBody?
    ): PostResponse

    @PUT("post/{id}")
    suspend fun updatePost(@Path("id") id: Int, @Body post: PostDto): PostResponse

    @DELETE("post/{id}")
    suspend fun deletePost(@Path("id") id: Int)

    @POST("post/like")
    suspend fun likePost(@Body likeRequest: LikeRequest): PostResponse
}