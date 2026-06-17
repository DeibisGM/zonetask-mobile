package com.app.zonetask.ui.screens.taskhistory

import com.app.zonetask.data.remote.dto.CompletedTaskHistoryResponse

data class CompletedTaskHistoryUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val items: List<CompletedTaskHistoryResponse> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val errorMessage: String? = null,
    val dateFrom: String = "",
    val dateTo: String = ""
)
