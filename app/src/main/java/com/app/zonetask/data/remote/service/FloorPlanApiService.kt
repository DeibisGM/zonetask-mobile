package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.CreateFloorPlanRequest
import com.app.zonetask.data.remote.dto.FloorPlanResponse
import com.app.zonetask.data.remote.dto.UpdateFloorPlanRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FloorPlanApiService {

    @GET(AppConstants.Api.Paths.SPACE_PLANS)
    suspend fun getPlansBySpace(
        @Path("spaceId") spaceId: Int
    ): Response<List<FloorPlanResponse>>

    @GET(AppConstants.Api.Paths.PLAN_BY_ID)
    suspend fun getPlanById(
        @Path("planId") planId: Int
    ): Response<FloorPlanResponse>

    @POST(AppConstants.Api.Paths.CREATE_PLAN)
    suspend fun createPlan(
        @Body request: CreateFloorPlanRequest
    ): Response<FloorPlanResponse>

    @PUT(AppConstants.Api.Paths.UPDATE_PLAN)
    suspend fun updatePlan(
        @Path("planId") planId: Int,
        @Body request: UpdateFloorPlanRequest
    ): Response<FloorPlanResponse>
}
