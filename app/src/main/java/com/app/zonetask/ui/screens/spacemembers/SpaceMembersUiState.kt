package com.app.zonetask.ui.screens.spacemembers

import com.app.zonetask.data.remote.dto.SpaceMemberWithUserDto
import com.app.zonetask.data.remote.dto.SpacePendingInvitationDto

data class SpaceMembersUiState(
    val members: List<SpaceMemberWithUserDto> = emptyList(),
    val pendingInvitations: List<SpacePendingInvitationDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredMembers: List<SpaceMemberWithUserDto>
        get() = if (searchQuery.isBlank()) members
        else members.filter { m ->
            m.displayName.contains(searchQuery, ignoreCase = true) ||
            m.role.contains(searchQuery, ignoreCase = true)
        }
}