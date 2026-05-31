package com.app.zonetask.navigation.plans

data class PlansNavActions(
    val onOpenList:   (spaceId: Int) -> Unit,
    val onCreatePlan: (spaceId: Int) -> Unit,
    val onOpenPlan:   (spaceId: Int, planId: Int) -> Unit,
    val onPlanSaved:  (message: String) -> Unit,
    val onBack:       () -> Unit
)
