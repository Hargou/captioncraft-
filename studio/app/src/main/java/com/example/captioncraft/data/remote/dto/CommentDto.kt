package com.example.captioncraft.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommentDto(
    val id: Int,
    val captionId: Int,
    val userId: Int,
    val username: String,
    val text: String,
    @SerializedName("created_at")
    val createdAt: String
) {
    fun getCreatedAtDate(): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(createdAt) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}

data class CommentCreateDto(
    val captionId: Int,
    val userId: Int,
    val password: String,
    val text: String
) 