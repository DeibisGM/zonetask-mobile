package com.app.zonetask.data.remote

import com.app.zonetask.core.AppConstants
import com.app.zonetask.data.remote.service.CompletionApiService
import com.app.zonetask.core.AuthSessionStore
import com.app.zonetask.data.remote.service.AuthApiService
import com.app.zonetask.data.remote.service.TaskLookupApiService
import com.app.zonetask.data.remote.service.TaskApiService
import com.app.zonetask.data.remote.service.SpaceApiService
import com.app.zonetask.data.remote.service.FloorPlanApiService
import com.app.zonetask.data.remote.service.InvitationApiService
import com.app.zonetask.data.remote.service.StatisticsApiService
import com.app.zonetask.data.remote.service.UserApiService
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Logs requests and responses while we build the task flow.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authHeaderInterceptor())
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AppConstants.Api.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Shared services use the same Retrofit instance.
    val spaceApiService: SpaceApiService by lazy {
        retrofit.create(SpaceApiService::class.java)
    }

    val floorPlanApiService: FloorPlanApiService by lazy {
        retrofit.create(FloorPlanApiService::class.java)
    }

    val taskLookupApiService: TaskLookupApiService by lazy {
        retrofit.create(TaskLookupApiService::class.java)
    }

    val taskApiService: TaskApiService by lazy {
        retrofit.create(TaskApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val completionApiService: CompletionApiService by lazy {
        retrofit.create(CompletionApiService::class.java)
    }

    val statisticsApiService: StatisticsApiService by lazy {
        retrofit.create(StatisticsApiService::class.java)
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val invitationApiService: InvitationApiService by lazy {
        retrofit.create(InvitationApiService::class.java)
    }

    private fun authHeaderInterceptor(): Interceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = AuthSessionStore.sessionToken
        val request = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(request)
    }
}