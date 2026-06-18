package com.app.zonetask.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.core.PlanDraftStore
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateFloorPlanRequest
import com.app.zonetask.data.remote.dto.UpdateFloorPlanRequest
import com.app.zonetask.data.remote.repository.ZoneRepository
import com.app.zonetask.data.repository.FloorPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PlanEditorViewModel(
    private val floorPlanRepository: FloorPlanRepository,
    private val zoneRepository: ZoneRepository,
    private val spaceId: Int,
    private val planId: Int?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlanEditorUiState(
            planId = planId,
            spaceId = spaceId
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        if (planId != null) {
            loadPlan(planId)
        } else {
            restoreDraftZones()
        }
    }

    private fun loadPlan(id: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorBanner = null)
        viewModelScope.launch {
            when (val planResult = floorPlanRepository.getPlanById(id)) {
                is ApiResult.Success -> {
                    val plan = planResult.data
                    when (val zonesResult = zoneRepository.getZonesByPlan(plan.planId)) {
                        is ApiResult.Success -> {
                            val zones = zonesResult.data
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                planId = plan.planId,
                                name = plan.name,
                                canvasWidth = plan.canvasWidth.toInt().toString(),
                                canvasHeight = plan.canvasHeight.toInt().toString(),
                                zones = zones,
                                selectedZoneId = zones.firstOrNull()?.id,
                                isDirty = false
                            )
                        }

                        is ApiResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorBanner = zonesResult.message
                            )
                        }
                    }
                }

                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorBanner = planResult.message
                    )
                }
            }
        }
    }

    private fun restoreDraftZones() {
        val storedZones = PlanDraftStore.loadZones(spaceId, null)
        val zones = if (storedZones.isNotEmpty()) {
            storedZones.map { it.toDraft() }
        } else {
            createStarterZones()
        }
        _uiState.value = _uiState.value.copy(
            zones = zones,
            selectedZoneId = zones.firstOrNull()?.id,
            isDirty = false
        )
        persistDraft(zones)
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, isDirty = true, errorBanner = null)
    }

    fun onCanvasWidthChange(value: String) {
        _uiState.value = _uiState.value.copy(canvasWidth = value, isDirty = true, errorBanner = null)
    }

    fun onCanvasHeightChange(value: String) {
        _uiState.value = _uiState.value.copy(canvasHeight = value, isDirty = true, errorBanner = null)
    }

    fun onAddZone() {
        val state = _uiState.value
        val nextIndex = state.zones.size + 1
        val paletteColor = PlanZonePalette[(nextIndex - 1) % PlanZonePalette.size]
        val baseName = "Zona $nextIndex"
        val existingNames = state.zones.map { it.name }
        val zone = PlanZoneDraft(
            name = uniqueZoneName(baseName, existingNames),
            x = 0.32f,
            y = 0.28f,
            width = 0.24f,
            height = 0.20f,
            fillColor = paletteColor
        )
        updateZones(state.zones + zone, selectedZoneId = zone.id)
    }

    fun onSelectZone(zoneId: String?) {
        _uiState.value = _uiState.value.copy(selectedZoneId = zoneId, errorBanner = null)
    }

    fun onSelectedZoneNameChange(value: String) = updateSelectedZone { zone ->
        zone.copy(name = value)
    }

    fun onSelectedZoneColorChange(value: String) = updateSelectedZone { zone ->
        zone.copy(fillColor = value)
    }

    fun onSelectedZoneOpacityChange(value: Float) = updateSelectedZone { zone ->
        zone.copy(opacity = value.coerceIn(0.35f, 1f))
    }

    fun onSelectedZoneSizeChange(width: Float, height: Float) = updateSelectedZone { zone ->
        zone.copy(
            width = width.coerceIn(0.08f, 0.90f),
            height = height.coerceIn(0.08f, 0.90f)
        )
    }

    fun onSelectedZonePositionChange(x: Float, y: Float) = updateSelectedZone { zone ->
        zone.copy(
            x = x.coerceIn(0f, 1f - zone.width),
            y = y.coerceIn(0f, 1f - zone.height)
        )
    }

    fun onDuplicateSelectedZone() {
        val state = _uiState.value
        val selected = state.zones.firstOrNull { it.id == state.selectedZoneId } ?: return
        val duplicated = selected.copy(
            id = UUID.randomUUID().toString(),
            backendId = null,
            name = uniqueZoneName("${selected.name} copia", state.zones.map { it.name }),
            x = (selected.x + 0.03f).coerceAtMost(0.80f),
            y = (selected.y + 0.03f).coerceAtMost(0.80f)
        )
        updateZones(state.zones + duplicated, selectedZoneId = duplicated.id)
    }

    fun onDeleteSelectedZone() {
        val state = _uiState.value
        val selectedId = state.selectedZoneId ?: return
        val remaining = state.zones.filterNot { it.id == selectedId }
        updateZones(remaining, selectedZoneId = remaining.firstOrNull()?.id)
    }

    fun onZoneDrag(zoneId: String, deltaX: Float, deltaY: Float) {
        val state = _uiState.value
        val updated = state.zones.map { zone ->
            if (zone.id != zoneId) {
                zone
            } else {
                zone.copy(
                    x = (zone.x + deltaX).coerceIn(0f, 1f - zone.width),
                    y = (zone.y + deltaY).coerceIn(0f, 1f - zone.height)
                )
            }
        }
        updateZones(updated, selectedZoneId = zoneId)
    }

    fun onZoneResize(zoneId: String, deltaWidth: Float, deltaHeight: Float) {
        val state = _uiState.value
        val updated = state.zones.map { zone ->
            if (zone.id != zoneId) {
                zone
            } else {
                zone.copy(
                    width = (zone.width + deltaWidth).coerceIn(0.08f, 0.92f - zone.x),
                    height = (zone.height + deltaHeight).coerceIn(0.08f, 0.92f - zone.y)
                )
            }
        }
        updateZones(updated, selectedZoneId = zoneId)
    }

    fun clearErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }

    fun save() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorBanner = "El nombre del plano es requerido")
            return
        }

        val width = state.canvasWidth.toFloatOrNull()
        val height = state.canvasHeight.toFloatOrNull()

        if (width == null || width <= 0f) {
            _uiState.value = state.copy(errorBanner = "El ancho del plano debe ser mayor a 0")
            return
        }
        if (height == null || height <= 0f) {
            _uiState.value = state.copy(errorBanner = "El alto del plano debe ser mayor a 0")
            return
        }
        if (state.zones.isEmpty()) {
            _uiState.value = state.copy(errorBanner = "Agrega al menos una zona antes de guardar")
            return
        }

        _uiState.value = state.copy(isSaving = true, errorBanner = null)

        viewModelScope.launch {
            val savedPlanResult = if (state.planId == null) {
                floorPlanRepository.createPlan(
                    CreateFloorPlanRequest(
                        name = state.name.trim(),
                        canvasWidth = width,
                        canvasHeight = height,
                        spaceId = state.spaceId
                    )
                )
            } else {
                floorPlanRepository.updatePlan(
                    state.planId,
                    UpdateFloorPlanRequest(
                        name = state.name.trim(),
                        canvasWidth = width,
                        canvasHeight = height
                    )
                )
            }

            when (savedPlanResult) {
                is ApiResult.Success -> {
                    val savedPlan = savedPlanResult.data
                    when (val savedZonesResult = zoneRepository.syncZones(savedPlan.planId, state.zones)) {
                        is ApiResult.Success -> {
                            val syncedZones = savedZonesResult.data
                            if (state.planId == null) {
                                PlanDraftStore.clearDraft(state.spaceId)
                            }
                            val selectedZoneId = syncedZones.firstOrNull { it.id == state.selectedZoneId }?.id
                                ?: syncedZones.firstOrNull()?.id
                            _uiState.value = state.copy(
                                planId = savedPlan.planId,
                                zones = syncedZones,
                                selectedZoneId = selectedZoneId,
                                isSaving = false,
                                isDirty = false,
                                isSaved = true
                            )
                        }

                        is ApiResult.Error -> {
                            _uiState.value = state.copy(
                                planId = savedPlan.planId,
                                isSaving = false,
                                errorBanner = savedZonesResult.message
                            )
                        }
                    }
                }

                is ApiResult.Error -> {
                    _uiState.value = state.copy(
                        isSaving = false,
                        errorBanner = savedPlanResult.message
                    )
                }
            }
        }
    }

    fun consumeSaved() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    private fun updateSelectedZone(transform: (PlanZoneDraft) -> PlanZoneDraft) {
        val state = _uiState.value
        val selectedId = state.selectedZoneId ?: return
        val updated = state.zones.map { zone ->
            if (zone.id == selectedId) transform(zone) else zone
        }
        updateZones(updated, selectedId)
    }

    private fun updateZones(zones: List<PlanZoneDraft>, selectedZoneId: String? = _uiState.value.selectedZoneId) {
        val nextState = _uiState.value.copy(
            zones = zones,
            selectedZoneId = selectedZoneId,
            isDirty = true,
            errorBanner = null
        )
        _uiState.value = nextState
        persistDraft(zones)
    }

    private fun persistDraft(zones: List<PlanZoneDraft>) {
        if (_uiState.value.planId != null) return
        PlanDraftStore.saveZones(
            spaceId = spaceId,
            planId = null,
            zones = zones.map { it.toSnapshot() }
        )
    }

    private fun uniqueZoneName(baseName: String, existingNames: List<String>): String {
        if (!existingNames.contains(baseName)) return baseName
        var suffix = 2
        while (existingNames.contains("$baseName $suffix")) {
            suffix++
        }
        return "$baseName $suffix"
    }
}

class PlanEditorViewModelFactory(
    private val floorPlanRepository: FloorPlanRepository,
    private val zoneRepository: ZoneRepository,
    private val spaceId: Int,
    private val planId: Int?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PlanEditorViewModel(
            floorPlanRepository = floorPlanRepository,
            zoneRepository = zoneRepository,
            spaceId = spaceId,
            planId = planId
        ) as T
}
