package com.cocido.nonna.data.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bus de invalidación centralizado para que las mutaciones refresquen
 * inmediatamente las pantallas que muestran los mismos datos.
 */
@Singleton
class DataRefreshCoordinator @Inject constructor() {

    sealed class Event {
        data object CofresList : Event()
        data class CofreDetail(val cofreId: String) : Event()
        data class MemoryDetail(val memoryId: String) : Event()
        data object Profile : Event()
        data object FamilyTree : Event()
        data object Notifications : Event()
    }

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 16)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    fun invalidateCofresList() {
        _events.tryEmit(Event.CofresList)
    }

    fun invalidateCofre(cofreId: String) {
        if (cofreId.isBlank()) return
        _events.tryEmit(Event.CofreDetail(cofreId))
        invalidateCofresList()
    }

    fun invalidateMemory(memoryId: String) {
        if (memoryId.isBlank()) return
        _events.tryEmit(Event.MemoryDetail(memoryId))
    }

    fun invalidateProfile() {
        _events.tryEmit(Event.Profile)
    }

    fun invalidateFamilyTree() {
        _events.tryEmit(Event.FamilyTree)
    }

    fun invalidateNotifications() {
        _events.tryEmit(Event.Notifications)
    }
}
