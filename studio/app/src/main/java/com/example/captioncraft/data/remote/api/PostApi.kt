package com.example.captioncraft.data.remote.api

import com.example.captioncraft.data.remote.dto.LikeRequest
import com.example.captioncraft.data.remote.dto.LikeResponse
import com.example.captioncraft.data.remote.dto.PostCreateResponse
import com.example.captioncraft.data.remote.dto.PostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PostApi {
    @GET("post")
    suspend fun getAllPosts(): Response<PostResponse>

    @GET("post")
    suspend fun getUserPosts(@Query("userId") userId: Int): Response<PostResponse>

    @Multipart
    @POST("post/create")
    suspend fun createPost(
        @Part("userId") userId: RequestBody,
        @Part("password") password: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("userCaptionText") caption: RequestBody? = null
    ): Response<PostCreateResponse>

    @POST("post/like")
    suspend fun likePost(
        @Body likeRequest: LikeRequest
    ): Response<LikeResponse>
}