package com.app.zonetask.core

object AppConstants {

    object Api {
        const val BASE_URL = "http://10.0.2.2:5248/"

        object Paths {
            const val USER_SPACES = "api/users/{userId}/spaces"
            const val SPACE_BY_ID = "spaces/{spaceId}"

            const val CREATE_SPACE = "spaces"
        }
    }
}