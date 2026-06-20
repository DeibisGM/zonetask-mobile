package com.app.zonetask.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.zonetask.core.PlanDraftStore
import com.app.zonetask.data.remote.ApiResult
import com.app.zonetask.data.remote.dto.CreateFloorPlanRequest
import com.app.zonetask.data.remote.dto.UpdateFloorPlanRequest
import com.app.zonetask.data.remote.repository.FloorPlanTemplateRepository
import com.app.zonetask.data.remote.repository.ZoneRepository
import com.app.zonetask.data.repository.FloorPlanRepository
import com.app.zonetask.domain.model.toDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PlanEditorViewModel(
    private val floorPlanRepository: FloorPlanRepository,
    private val zoneRepository: ZoneRepository,
    private val templateRepository: FloorPlanTemplateRepository,
    private val spaceId: Int,
    private val planId: Int?,
    private val templateId: Int? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlanEditorUiState(planId = planId, spaceId = spaceId, templateId = templateId))
    val uiState = _uiState.asStateFlow()
    private var selectedZoneSnapshot: GridZoneGeometry? = null

    init {
        when {
            planId != null    -> loadPlan(planId)
            templateId != null -> loadTemplate(templateId)
            else              -> Unit
        }
    }

    fun completeSetup(name: String, columns: String, rows: String) {
        val parsedColumns = columns.toIntOrNull()
        val parsedRows = rows.toIntOrNull()
        if (name.isBlank()) return setError("Floor name is required")
        if (parsedColumns == null || parsedColumns !in 6..1000) return setError("Width must be between 6 and 1000 cells")
        if (parsedRows == null || parsedRows !in 6..1000) return setError("Height must be between 6 and 1000 cells")
        val grid = FloorGridSpec(parsedColumns, parsedRows)
        val state = _uiState.value.copy(
            name = name.trim(), canvasWidth = parsedColumns.toString(), canvasHeight = parsedRows.toString(),
            setupComplete = true, zones = normalizeZones(_uiState.value.zones, grid), isDirty = true, errorBanner = null
        )
        _uiState.value = state
    }

    fun onSelectZone(zoneId: String?) {
        val state = _uiState.value
        val previousSelected = state.selectedZoneId
        if (previousSelected != null && previousSelected != zoneId) {
            finalizeSelection(previousSelected)
        }
        if (zoneId != null && (zoneId != previousSelected || selectedZoneSnapshot == null)) {
            selectedZoneSnapshot = state.zones.firstOrNull { it.id == zoneId }?.geometry(gridFor(state))
        }
        updateState { it.copy(selectedZoneId = zoneId, errorBanner = null) }
    }

    fun onCreateRoom(column: Int, row: Int, spanColumns: Int, spanRows: Int) {
        val state = _uiState.value
        val grid = gridFor(state)
        val geometry = GridZoneGeometry(column, row, spanColumns, spanRows).bounded(grid)
        if (!isAreaFree(geometry, state.zones, grid)) return setError("Rooms cannot overlap")
        val index = state.zones.size + 1
        val zone = geometry.toDraftGeometry(
            PlanZoneDraft(
                id = UUID.randomUUID().toString(), name = uniqueName("Room $index", state.zones),
                x = 0f, y = 0f, width = 0f, height = 0f, fillColor = PlanZonePalette[(index - 1) % PlanZonePalette.size]
            ), grid
        )
        replaceZones(state.zones + zone, zone.id)
    }

    fun onAddZone() {
        val state = _uiState.value
        val grid = gridFor(state)
        val spanColumns = 12
        val spanRows = 12
        val centerColumn = ((grid.columns - spanColumns) / 2).coerceAtLeast(0)
        val centerRow = ((grid.rows - spanRows) / 2).coerceAtLeast(0)
        val source = GridZoneGeometry(centerColumn, centerRow, spanColumns, spanRows).bounded(grid)
        val geometry = if (isAreaFree(source, state.zones, grid)) source else findNearestFree(source, state.zones, grid) ?: run {
            setError("No free space for a new zone")
            return
        }
        val index = state.zones.size + 1
        val zone = geometry.toDraftGeometry(
            PlanZoneDraft(
                id = UUID.randomUUID().toString(),
                name = uniqueName("Room $index", state.zones),
                x = 0f,
                y = 0f,
                width = 0f,
                height = 0f,
                fillColor = PlanZonePalette[(index - 1) % PlanZonePalette.size]
            ),
            grid
        )
        replaceZones(state.zones + zone, zone.id)
    }

    fun onMoveZone(id: String, column: Int, row: Int) = mutateGeometry(id, validateOverlap = false) { zone, grid ->
        GridZoneGeometry(column, row, zone.geometry(grid).spanColumns, zone.geometry(grid).spanRows).bounded(grid)
    }

    fun onResizeZone(id: String, spanColumns: Int, spanRows: Int) = mutateGeometry(id, validateOverlap = false) { zone, grid ->
        val old = zone.geometry(grid)
        GridZoneGeometry(old.column, old.row, spanColumns, spanRows).bounded(grid)
    }

    fun onZoneGeometryChanged(id: String, column: Int, row: Int, spanColumns: Int, spanRows: Int) = mutateGeometry(id, validateOverlap = false) { _, grid ->
        GridZoneGeometry(column, row, spanColumns, spanRows).bounded(grid)
    }

    fun onSelectedZoneNameChange(value: String) = mutateSelected { it.copy(name = value.trim().ifBlank { it.name }) }
    fun onSelectedZoneColorChange(value: String) = mutateSelected { it.copy(fillColor = value) }

    fun onDuplicateSelectedZone() {
        val state = _uiState.value
        val selected = state.zones.firstOrNull { it.id == state.selectedZoneId } ?: return
        val grid = gridFor(state)
        val source = selected.geometry(grid)
        val candidate = findNearestFree(source, state.zones, grid) ?: return setError("No free space for a copy")
        val duplicate = candidate.toDraftGeometry(
            selected.copy(id = UUID.randomUUID().toString(), backendId = null, name = uniqueName("${selected.name} copy", state.zones)), grid
        )
        replaceZones(state.zones + duplicate, duplicate.id)
    }

    fun onDeleteSelectedZone() {
        val state = _uiState.value
        selectedZoneSnapshot = null
        val remaining = state.zones.filterNot { it.id == state.selectedZoneId }
        replaceZones(remaining, remaining.firstOrNull()?.id)
    }

    fun clearErrorBanner() = updateState { it.copy(errorBanner = null) }

    fun save() {
        finalizeSelection(_uiState.value.selectedZoneId)
        val state = _uiState.value
        if (!state.setupComplete) return setError("Finish floor setup first")
        val grid = gridFor(state)
        _uiState.value = state.copy(isSaving = true, errorBanner = null)
        viewModelScope.launch {
            val savedPlan = if (state.planId == null) {
                floorPlanRepository.createPlan(
                    CreateFloorPlanRequest(state.name, grid.columns.toFloat(), grid.rows.toFloat(), state.spaceId, templateId = state.templateId)
                )
            } else {
                floorPlanRepository.updatePlan(state.planId, UpdateFloorPlanRequest(state.name, grid.columns.toFloat(), grid.rows.toFloat()))
            }
            when (savedPlan) {
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isSaving = false, errorBanner = savedPlan.message)
                is ApiResult.Success -> when (val synced = zoneRepository.syncZones(
                    savedPlan.data.planId, state.zones, savedPlan.data.canvasWidth, savedPlan.data.canvasHeight
                )) {
                    is ApiResult.Error -> _uiState.value = _uiState.value.copy(planId = savedPlan.data.planId, isSaving = false, errorBanner = synced.message)
                    is ApiResult.Success -> {
                        val zones = normalizeZones(synced.data, grid)
                        PlanDraftStore.clearDraft(spaceId, null)
                        PlanDraftStore.clearDraft(spaceId, savedPlan.data.planId)
                        _uiState.value = _uiState.value.copy(
                            planId = savedPlan.data.planId, zones = zones,
                            selectedZoneId = zones.firstOrNull { it.id == state.selectedZoneId }?.id,
                            isSaving = false, isDirty = false, isSaved = true
                        )
                    }
                }
            }
        }
    }

    fun consumeSaved() = updateState { it.copy(isSaved = false) }

    private fun loadPlan(id: Int) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        when (val planResult = floorPlanRepository.getPlanById(id)) {
            is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorBanner = planResult.message)
            is ApiResult.Success -> when (val zonesResult = zoneRepository.getZonesByPlan(
                planResult.data.planId, planResult.data.canvasWidth, planResult.data.canvasHeight
            )) {
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorBanner = zonesResult.message)
                is ApiResult.Success -> {
                    val columns = gridDimension(planResult.data.canvasWidth.toInt().toString(), 240)
                    val rows = gridDimension(planResult.data.canvasHeight.toInt().toString(), 240)
                    val grid = FloorGridSpec(columns, rows)
                    val zones = normalizeZones(zonesResult.data, grid)
                    selectedZoneSnapshot = zones.firstOrNull()?.geometry(grid)
                    _uiState.value = PlanEditorUiState(
                        planId = planResult.data.planId, spaceId = spaceId,
                        name = planResult.data.name,
                        canvasWidth = columns.toString(), canvasHeight = rows.toString(), setupComplete = true,
                        zones = zones, selectedZoneId = zones.firstOrNull()?.id,
                        isDirty = false
                    )
                }
            }
        }
    }

    private fun loadTemplate(id: Int) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoadingTemplate = true)
        when (val result = templateRepository.getById(id)) {
            is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoadingTemplate = false, errorBanner = result.message)
            is ApiResult.Success -> {
                val template = result.data
                val drafts = template.zones.map { it.toDraft() }
                _uiState.value = _uiState.value.copy(
                    isLoadingTemplate = false,
                    templateId = id,
                    name = template.name,
                    canvasWidth = template.defaultColumns.toString(),
                    canvasHeight = template.defaultRows.toString(),
                    zones = drafts
                )
            }
        }
    }

    private fun mutateGeometry(
        id: String,
        validateOverlap: Boolean = true,
        transform: (PlanZoneDraft, FloorGridSpec) -> GridZoneGeometry
    ) {
        val state = _uiState.value
        val grid = gridFor(state)
        val zone = state.zones.firstOrNull { it.id == id } ?: return
        val geometry = transform(zone, grid)
        if (validateOverlap && !isAreaFree(geometry, state.zones, grid, id)) return setError("Rooms cannot overlap")
        replaceZones(state.zones.map { if (it.id == id) geometry.toDraftGeometry(it, grid) else it }, id)
    }

    private fun mutateSelected(transform: (PlanZoneDraft) -> PlanZoneDraft) {
        val selected = _uiState.value.selectedZoneId ?: return
        replaceZones(_uiState.value.zones.map { if (it.id == selected) transform(it) else it }, selected)
    }

    private fun replaceZones(zones: List<PlanZoneDraft>, selectedId: String?) = updateState {
        it.copy(zones = normalizeZones(zones, gridFor(it)), selectedZoneId = selectedId, isDirty = true, errorBanner = null)
    }

    private fun updateState(transform: (PlanEditorUiState) -> PlanEditorUiState) {
        _uiState.value = transform(_uiState.value)
        persistDraft()
    }

    private fun finalizeSelection(selectedId: String?) {
        if (selectedId == null) {
            selectedZoneSnapshot = null
            return
        }
        val state = _uiState.value
        val grid = gridFor(state)
        val zone = state.zones.firstOrNull { it.id == selectedId } ?: run {
            selectedZoneSnapshot = null
            return
        }
        val current = zone.geometry(grid)
        val overlaps = !isAreaFree(current, state.zones, grid, selectedId)
        if (overlaps) {
            val snapshot = selectedZoneSnapshot
            if (snapshot != null) {
                val reverted = state.zones.map { if (it.id == selectedId) snapshot.toDraftGeometry(it, grid) else it }
                _uiState.value = state.copy(zones = normalizeZones(reverted, grid), isDirty = true, errorBanner = null)
            }
        }
        selectedZoneSnapshot = null
    }

    private fun persistDraft() {
        // Draft persistence is intentionally disabled. Save is explicit and only happens through the top-bar action.
    }

    private fun gridFor(state: PlanEditorUiState) = FloorGridSpec(gridDimension(state.canvasWidth, 240), gridDimension(state.canvasHeight, 240))
    private fun gridDimension(value: String?, fallback: Int) = value?.toIntOrNull()?.takeIf { it in 6..1000 } ?: fallback
    private fun normalizeZones(zones: List<PlanZoneDraft>, grid: FloorGridSpec) = zones.map { it.geometry(grid).toDraftGeometry(it, grid) }
    private fun setError(message: String) = updateState { it.copy(errorBanner = message) }

    private fun isAreaFree(candidate: GridZoneGeometry, zones: List<PlanZoneDraft>, grid: FloorGridSpec, ignoredId: String? = null): Boolean =
        zones.filter { it.id != ignoredId }.none { candidate.intersects(it.geometry(grid)) }

    private fun GridZoneGeometry.intersects(other: GridZoneGeometry): Boolean =
        column < other.column + other.spanColumns && column + spanColumns > other.column &&
            row < other.row + other.spanRows && row + spanRows > other.row

    private fun findNearestFree(source: GridZoneGeometry, zones: List<PlanZoneDraft>, grid: FloorGridSpec): GridZoneGeometry? {
        for (distance in 1..maxOf(grid.columns, grid.rows)) {
            for (dx in -distance..distance) for (dy in -distance..distance) {
                if (kotlin.math.abs(dx) != distance && kotlin.math.abs(dy) != distance) continue
                val candidate = GridZoneGeometry(source.column + dx, source.row + dy, source.spanColumns, source.spanRows).bounded(grid)
                if (candidate != source && isAreaFree(candidate, zones, grid)) return candidate
            }
        }
        return null
    }

    private fun uniqueName(base: String, zones: List<PlanZoneDraft>): String {
        val names = zones.map { it.name }.toSet()
        if (base !in names) return base
        var suffix = 2
        while ("$base $suffix" in names) suffix++
        return "$base $suffix"
    }
}

class PlanEditorViewModelFactory(
    private val floorPlanRepository: FloorPlanRepository,
    private val zoneRepository: ZoneRepository,
    private val templateRepository: FloorPlanTemplateRepository,
    private val spaceId: Int,
    private val planId: Int?,
    private val templateId: Int? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PlanEditorViewModel(floorPlanRepository, zoneRepository, templateRepository, spaceId, planId, templateId) as T
}
