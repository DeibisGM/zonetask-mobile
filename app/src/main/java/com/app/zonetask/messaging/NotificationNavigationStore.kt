package com.app.zonetask.messaging

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicReference

object NotificationNavigationStore {

    private val lastRoute = AtomicReference<String?>(null)
    private val routeEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val events = routeEvents.asSharedFlow()

    fun postRoute(route: String) {
        lastRoute.set(route)
        routeEvents.tryEmit(route)
    }

    fun consumeLastRoute(): String? = lastRoute.getAndSet(null)
}
