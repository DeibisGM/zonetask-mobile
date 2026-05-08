package com.app.zonetask.di

import com.app.zonetask.data.remote.RetrofitClient
import com.app.zonetask.data.repository.SpaceRepository

object AppContainer {

    val spaceRepository: SpaceRepository by lazy {
        SpaceRepository(RetrofitClient.spaceApiService)
    }
}