package com.app.zonetask.ui.screens.taskcreate

data class TaskFormOptionsUiState(
    val categories: List<Pair<String, String>> = emptyList(),
    val zones: List<Pair<String, String>> = emptyList(),
    val objects: List<Pair<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val objectsLoading: Boolean = false,
    val errorMessage: String? = null
)
