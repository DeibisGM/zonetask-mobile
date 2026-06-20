package com.app.zonetask.ui.screens.plan

import org.junit.Assert.assertEquals
import org.junit.Test

class GridZoneGeometryTest {
    @Test
    fun `bounds clamp a room to the floor grid`() {
        val geometry = GridZoneGeometry(column = 22, row = 17, spanColumns = 4, spanRows = 3)
            .bounded(FloorGridSpec(columns = 24, rows = 18))

        assertEquals(20, geometry.column)
        assertEquals(15, geometry.row)
        assertEquals(4, geometry.spanColumns)
        assertEquals(3, geometry.spanRows)
    }

    @Test
    fun `grid geometry converts to normalized api geometry`() {
        val grid = FloorGridSpec(columns = 24, rows = 18)
        val zone = GridZoneGeometry(6, 3, 8, 5).toDraftGeometry(
            PlanZoneDraft("Room", 0f, 0f, 0f, 0f, "#76D6D0"), grid
        )

        assertEquals(0.25f, zone.x, 0.0001f)
        assertEquals(1f / 6f, zone.y, 0.0001f)
        assertEquals(1f / 3f, zone.width, 0.0001f)
        assertEquals(5f / 18f, zone.height, 0.0001f)
        assertEquals(10f, zone.geometry(grid).centerColumn, 0.0001f)
    }

    @Test
    fun `legacy normalized geometry is restored onto whole cells`() {
        val grid = FloorGridSpec(columns = 24, rows = 18)
        val zone = PlanZoneDraft("Room", 0.25f, 1f / 6f, 1f / 3f, 5f / 18f, "#76D6D0")
        val geometry = zone.geometry(grid)

        assertEquals(GridZoneGeometry(6, 3, 8, 5), geometry)
    }
}
