package com.example.captioncraft.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LikeRequest(
    // The server requires these exact field names
    val postId: Int,
    val userId: Int,
    val password: String
) 