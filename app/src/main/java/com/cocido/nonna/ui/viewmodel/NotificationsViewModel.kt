package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.NotificationUiModel
import com.cocido.nonna.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.os.SystemClock
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {
    private val cacheTtlMs = 20_000L
    private var lastLoadAtMs: Long = 0L

    private val _items = MutableStateFlow<List<NotificationUiModel>>(emptyList())
    val items: StateFlow<List<NotificationUiModel>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _onlyUnread = MutableStateFlow(false)
    val onlyUnread: StateFlow<Boolean> = _onlyUnread.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _markingReadIds = MutableStateFlow<Set<Long>>(emptySet())
    val markingReadIds: StateFlow<Set<Long>> = _markingReadIds.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private val pageSize = 20

    fun load(forceRefresh: Boolean = false) {
        if (_isLoading.value || _isLoadingMore.value) return
        val now = SystemClock.elapsedRealtime()
        if (!forceRefresh && (now - lastLoadAtMs) <= cacheTtlMs && _items.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            if (forceRefresh) {
                currentPage = 1
                totalPages = 1
                _items.value = emptyList()
            }
            when (
                val result = notificationsRepository.getMyNotifications(
                    pageNumber = currentPage,
                    pageSize = pageSize,
                    onlyUnread = _onlyUnread.value
                )
            ) {
                is ApiResult.Success -> {
                    totalPages = result.data.totalPages.coerceAtLeast(1)
                    _items.value = result.data.items
                    lastLoadAtMs = SystemClock.elapsedRealtime()
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun loadMore() {
        if (_isLoading.value || _isLoadingMore.value) return
        if (currentPage >= totalPages) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1
            when (
                val result = notificationsRepository.getMyNotifications(
                    pageNumber = nextPage,
                    pageSize = pageSize,
                    onlyUnread = _onlyUnread.value
                )
            ) {
                is ApiResult.Success -> {
                    currentPage = nextPage
                    totalPages = result.data.totalPages.coerceAtLeast(1)
                    _items.value = _items.value + result.data.items
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> Unit
            }
            _isLoadingMore.value = false
        }
    }

    fun toggleOnlyUnread(enabled: Boolean) {
        if (_onlyUnread.value == enabled) return
        _onlyUnread.value = enabled
        currentPage = 1
        totalPages = 1
        load(forceRefresh = true)
    }

    fun markAsRead(id: Long) {
        if (id <= 0L) return
        if (_markingReadIds.value.contains(id)) return
        viewModelScope.launch {
            _markingReadIds.value = _markingReadIds.value + id
            when (val result = notificationsRepository.markAsRead(id)) {
                is ApiResult.Success -> {
                    _items.value = _items.value.map { item ->
                        if (item.id == id) item.copy(isRead = true) else item
                    }
                }
                is ApiResult.Error -> _errorMessage.value = result.message
                else -> Unit
            }
            _markingReadIds.value = _markingReadIds.value - id
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun refreshOnResume(minIntervalMs: Long = 2000L) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastLoadAtMs < minIntervalMs && _items.value.isNotEmpty()) return
        load(forceRefresh = true)
    }
}

