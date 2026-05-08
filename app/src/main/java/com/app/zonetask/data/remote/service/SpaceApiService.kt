package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.SpaceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SpaceApiService {

    @GET(AppConstants.Api.Paths.USER_SPACES)
    suspend fun getSpacesByUser(
        @Path("userId") userId: Int
    ): Response<List<SpaceResponse>>

    @GET(AppConstants.Api.Paths.SPACE_BY_ID)
    suspend fun getSpaceById(
        @Path("spaceId") spaceId: Int
    ): Response<SpaceResponse>
}
