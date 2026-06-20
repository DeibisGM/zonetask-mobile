package com.app.zonetask.data.remote.dto

import com.app.zonetask.ui.screens.plan.PlanZoneDraft
import org.junit.Assert.assertEquals
import org.junit.Test

class ZoneCoordinateMappingTest {
    @Test
    fun `zone request uses floor plan units required by the database`() {
        val request = PlanZoneDraft("Kitchen", 0.25f, 0.5f, 0.4f, 0.25f, "#76D6D0")
            .toRequest(displayOrder = 0, canvasWidth = 1200f, canvasHeight = 800f)

        assertEquals(300f, request.posX, 0.001f)
        assertEquals(400f, request.posY, 0.001f)
        assertEquals(480f, request.width, 0.001f)
        assertEquals(200f, request.height, 0.001f)
    }

    @Test
    fun `zone response is normalized against its owning floor`() {
        val response = ZoneResponse(1, "Kitchen", 300f, 400f, 480f, 200f, "rectangle", null, "#76D6D0", "#111111", 2f, 0.92f, 0, 1)
        val draft = response.toDraft(canvasWidth = 1200f, canvasHeight = 800f)

        assertEquals(0.25f, draft.x, 0.001f)
        assertEquals(0.5f, draft.y, 0.001f)
        assertEquals(0.4f, draft.width, 0.001f)
        assertEquals(0.25f, draft.height, 0.001f)
    }
}
