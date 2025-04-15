package com.example.captioncraft.data.repository

import com.example.captioncraft.data.local.dao.FollowDao
import com.example.captioncraft.data.remote.api.FollowApi
import javax.inject.Inject

class FollowRepository @Inject constructor(
    private val followDao: FollowDao,
    private val followApi: FollowApi
) {
}