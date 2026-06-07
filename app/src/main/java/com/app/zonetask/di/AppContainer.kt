package com.app.zonetask.di

import com.app.zonetask.data.auth.FirebaseAuthRepository
import com.app.zonetask.data.remote.RetrofitClient
import com.app.zonetask.data.remote.repository.TaskLookupRepository
import com.app.zonetask.data.remote.repository.TaskRepository
import com.app.zonetask.data.remote.repository.UserRepository
import com.app.zonetask.data.repository.FloorPlanRepository
import com.app.zonetask.data.repository.SpaceRepository

object AppContainer {

    // Small manual DI setup for the screens in this module.
    val spaceRepository: SpaceRepository by lazy {
        SpaceRepository(RetrofitClient.spaceApiService)
    }

    val floorPlanRepository: FloorPlanRepository by lazy {
        FloorPlanRepository(RetrofitClient.floorPlanApiService)
    }

    val taskLookupRepository: TaskLookupRepository by lazy {
        TaskLookupRepository(RetrofitClient.taskLookupApiService)
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(RetrofitClient.taskApiService)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(RetrofitClient.userApiService)
    }

    val authRepository: FirebaseAuthRepository by lazy {
        FirebaseAuthRepository()
    }
}
