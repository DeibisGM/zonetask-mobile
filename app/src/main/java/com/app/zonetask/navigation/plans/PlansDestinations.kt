package com.app.zonetask.navigation.plans

object PlansDestinations {

    const val ARG_SPACE_ID   = "spaceId"
    const val ARG_PLAN_ID    = "planId"
    const val ARG_TEMPLATE_ID = "templateId"

    const val LIST            = "space_plans/{$ARG_SPACE_ID}"
    const val TEMPLATE_SELECT = "plan_template_select/{$ARG_SPACE_ID}"
    const val NEW             = "plan_editor/{$ARG_SPACE_ID}?$ARG_TEMPLATE_ID={$ARG_TEMPLATE_ID}"
    const val EDITOR          = "plan_editor/{$ARG_SPACE_ID}/{$ARG_PLAN_ID}"

    fun list(spaceId: Int): String             = "space_plans/$spaceId"
    fun templateSelect(spaceId: Int): String   = "plan_template_select/$spaceId"
    fun newPlan(spaceId: Int, templateId: Int? = null): String =
        if (templateId != null) "plan_editor/$spaceId?$ARG_TEMPLATE_ID=$templateId"
        else "plan_editor/$spaceId"
    fun editor(spaceId: Int, planId: Int): String = "plan_editor/$spaceId/$planId"
}

object PlansNavKeys {
    const val PLAN_SAVED_MESSAGE = "plans_saved_message"
    const val RELOAD_PLANS       = "plans_reload"
}
