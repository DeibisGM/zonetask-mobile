package com.app.zonetask.data.remote.service

import com.app.zonetask.data.remote.dto.FloorPlanTemplateResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FloorPlanTemplateApiService {

    @GET("api/templates")
    suspend fun getAll(): Response<List<FloorPlanTemplateResponse>>

    @GET("api/templates/{id}")
    suspend fun getById(@Path("id") id: Int): Response<FloorPlanTemplateResponse>
}
