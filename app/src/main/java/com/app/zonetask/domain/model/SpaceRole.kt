package com.app.zonetask.domain.model

enum class SpaceRole {
    OWNER, ADMIN, MEMBER;

    companion object {
        fun from(raw: String): SpaceRole = when (raw.lowercase()) {
            "owner" -> OWNER
            "admin" -> ADMIN
            else    -> MEMBER
        }
    }
}