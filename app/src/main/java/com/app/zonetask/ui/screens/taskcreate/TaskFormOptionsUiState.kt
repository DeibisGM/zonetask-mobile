package com.app.zonetask.ui.screens.taskcreate

data class TaskFormOptionsUiState(
    // Dropdown options loaded from the backend.
    val categories: List<Pair<String, String>> = emptyList(),
    val zones: List<Pair<String, String>> = emptyList(),
    // Checkbox options for the selected zone.
    val objects: List<Pair<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val objectsLoading: Boolean = false,
    val errorMessage: String? = null
)
