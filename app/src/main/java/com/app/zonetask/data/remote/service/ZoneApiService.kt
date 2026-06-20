package com.app.zonetask.data.remote.service

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.dto.ZoneRequest
import com.app.zonetask.data.remote.dto.ZoneResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ZoneApiService {

    @GET(AppConstants.Api.Paths.PLAN_ZONES)
    suspend fun getZonesByPlan(
        @Path("planId") planId: Int
    ): Response<List<ZoneResponse>>

    @GET(AppConstants.Api.Paths.ZONE_BY_ID)
    suspend fun getZoneById(
        @Path("zoneId") zoneId: Int
    ): Response<ZoneResponse>

    @POST(AppConstants.Api.Paths.PLAN_ZONES)
    suspend fun createZone(
        @Path("planId") planId: Int,
        @Body request: ZoneRequest
    ): Response<ZoneResponse>

    @PUT(AppConstants.Api.Paths.ZONE_BY_ID)
    suspend fun updateZone(
        @Path("zoneId") zoneId: Int,
        @Body request: ZoneRequest
    ): Response<ZoneResponse>

    @DELETE(AppConstants.Api.Paths.ZONE_BY_ID)
    suspend fun deleteZone(
        @Path("zoneId") zoneId: Int
    ): Response<Unit>
}
