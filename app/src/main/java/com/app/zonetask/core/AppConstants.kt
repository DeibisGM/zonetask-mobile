package com.app.zonetask.core

object AppConstants {

    object Api {
        const val BASE_URL = "http://192.168.100.130:5248/"

        object Paths {
            const val USER_SPACES = "users/{userId}/spaces"
            const val SPACE_BY_ID = "spaces/{spaceId}"
        }
    }
}