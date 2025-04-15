package com.example.captioncraft.data.remote.api

import com.example.captioncraft.data.remote.dto.CaptionCreateDto
import com.example.captioncraft.data.remote.dto.CaptionCreateResponse
import com.example.captioncraft.data.remote.dto.CaptionDto
import com.example.captioncraft.data.remote.dto.CaptionLikeRequest
import com.example.captioncraft.data.remote.dto.CaptionResponse
import com.example.captioncraft.data.remote.dto.CommentCreateDto
import com.example.captioncraft.data.remote.dto.CommentDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CaptionApi {

    @GET("captions/post/{postId}")
    suspend fun getCaptions(@Path("postId") postId: Int): CaptionResponse

    @POST("captions")
    suspend fun createCaption(@Body caption: CaptionCreateDto): CaptionCreateResponse

    @POST("captions/like")
    suspend fun likeCaption(@Body likeRequest: CaptionLikeRequest)

    @GET("captions/{captionId}")
    suspend fun getCaptionById(@Path("captionId") captionId: Int): CaptionDto

    @GET("captions")
    suspend fun getAllCaptions(): List<CaptionDto>
    
    // Comment endpoints
    @GET("captions/comments/{captionId}")
    suspend fun getComments(@Path("captionId") captionId: Int): List<CommentDto>
    
    @POST("captions/comment")
    suspend fun addComment(@Body comment: CommentCreateDto): CommentDto
}
