package com.app.zonetask.navigation.plans

object PlansDestinations {

    const val ARG_SPACE_ID = "spaceId"
    const val ARG_PLAN_ID  = "planId"

    const val LIST   = "space_plans/{$ARG_SPACE_ID}"
    const val NEW    = "plan_editor/{$ARG_SPACE_ID}"
    const val EDITOR = "plan_editor/{$ARG_SPACE_ID}/{$ARG_PLAN_ID}"

    fun list(spaceId: Int): String              = "space_plans/$spaceId"
    fun newPlan(spaceId: Int): String           = "plan_editor/$spaceId"
    fun editor(spaceId: Int, planId: Int): String = "plan_editor/$spaceId/$planId"
}

object PlansNavKeys {
    const val PLAN_SAVED_MESSAGE = "plans_saved_message"
    const val RELOAD_PLANS       = "plans_reload"
}
