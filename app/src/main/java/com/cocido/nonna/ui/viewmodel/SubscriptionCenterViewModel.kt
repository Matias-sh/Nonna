package com.cocido.nonna.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import android.content.Context
import com.cocido.nonna.analytics.AnalyticsEvent
import com.cocido.nonna.analytics.AppAnalytics
import com.cocido.nonna.analytics.PaymentsAnalytics
import com.cocido.nonna.data.remote.dto.PagoSuscripcionResponseDto
import com.cocido.nonna.data.remote.dto.PeriodicidadPago
import com.cocido.nonna.data.remote.dto.SuscripcionActualDto
import com.cocido.nonna.data.remote.dto.SuscripcionPlanDto
import com.cocido.nonna.data.repository.ApiResult
import com.cocido.nonna.data.repository.PagosSuscripcionRepository
import com.cocido.nonna.data.repository.PlanesRepository
import com.cocido.nonna.data.repository.SuscripcionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class SubscriptionCenterViewModel @Inject constructor(
    private val suscripcionRepository: SuscripcionRepository,
    private val planesRepository: PlanesRepository,
    private val pagosRepository: PagosSuscripcionRepository,
    private val analytics: AppAnalytics,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private companion object {
        const val TAG = "SubscriptionCenterVM"
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isProcessingCheckout = MutableStateFlow(false)
    val isProcessingCheckout: StateFlow<Boolean> = _isProcessingCheckout.asStateFlow()

    private val _suscripcion = MutableStateFlow<SuscripcionActualDto?>(null)
    val suscripcion: StateFlow<SuscripcionActualDto?> = _suscripcion.asStateFlow()

    private val _planes = MutableStateFlow<List<SuscripcionPlanDto>>(emptyList())
    val planes: StateFlow<List<SuscripcionPlanDto>> = _planes.asStateFlow()

    private val _pagos = MutableStateFlow<List<PagoSuscripcionResponseDto>>(emptyList())
    val pagos: StateFlow<List<PagoSuscripcionResponseDto>> = _pagos.asStateFlow()

    private val _pendingPayment = MutableStateFlow<PagoSuscripcionResponseDto?>(null)
    val pendingPayment: StateFlow<PagoSuscripcionResponseDto?> = _pendingPayment.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _checkoutUrl = MutableSharedFlow<String>()
    val checkoutUrl: SharedFlow<String> = _checkoutUrl.asSharedFlow()

    private val _infoMessage = MutableSharedFlow<String>()
    val infoMessage: SharedFlow<String> = _infoMessage.asSharedFlow()
    private var lastPassiveRefreshAt: Long = 0L
    private var pendingPollingJob: Job? = null
    private var pagosRequestSeq: Long = 0L

    private val prefs by lazy {
        context.getSharedPreferences("nonna_payments_flow", Context.MODE_PRIVATE)
    }

    fun load() {
        viewModelScope.launch {
            analytics.track(AnalyticsEvent(PaymentsAnalytics.SCREEN_OPENED))
            _isLoading.value = true
            loadSuscripcion()
            loadPlanes()
            loadPagos()
            loadPagoPendiente()
            maybeStartPendingPolling()
            _isLoading.value = false
        }
    }

    fun startCheckout(plan: SuscripcionPlanDto, periodicidad: PeriodicidadPago, autoRenovar: Boolean = true) {
        val pending = _pendingPayment.value
        if (pending != null && isBlockingPending(pending)) {
            continuePendingCheckout()
            return
        }
        val planId = plan.id ?: run {
            viewModelScope.launch { _errorMessage.emit("El plan no tiene ID válido.") }
            return
        }
        viewModelScope.launch {
            _isProcessingCheckout.value = true
            analytics.track(
                AnalyticsEvent(
                    PaymentsAnalytics.CHECKOUT_TAP,
                    mapOf(
                        "plan_id" to planId.toString(),
                        "periodicidad" to periodicidad.apiValue,
                        "auto_renovar" to autoRenovar.toString()
                    )
                )
            )
            Log.d(TAG, "Checkout start -> planId=$planId periodicidad=${periodicidad.apiValue} autoRenovar=$autoRenovar")
            when (val result = pagosRepository.crearCheckout(planId, periodicidad, autoRenovar)) {
                is ApiResult.Success -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.CHECKOUT_CREATE_SUCCESS,
                            mapOf(
                                "pago_id" to result.data.pagoId.toString(),
                                "preference_id" to result.data.preferenceId
                            )
                        )
                    )
                    saveLastCheckoutPagoId(result.data.pagoId)
                    _checkoutUrl.emit(result.data.initPoint)
                }
                is ApiResult.Error -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.CHECKOUT_CREATE_ERROR,
                            mapOf("message" to result.message)
                        )
                    )
                    _errorMessage.emit(result.message)
                }
                else -> Unit
            }
            _isProcessingCheckout.value = false
        }
    }

    fun continuePendingCheckout() {
        val currentPending = _pendingPayment.value ?: run {
            viewModelScope.launch { _errorMessage.emit("No hay un pago pendiente para continuar.") }
            return
        }
        viewModelScope.launch {
            if (!isBlockingPending(currentPending)) {
                _infoMessage.emit("Este pago ya no está pendiente. Actualizamos tu estado.")
                refreshAfterCheckout()
                return@launch
            }

            analytics.track(
                AnalyticsEvent(
                    PaymentsAnalytics.PENDING_CHECKOUT_CONTINUE_TAP,
                    mapOf("pago_id" to currentPending.id.toString())
                )
            )

            // Antes de reabrir checkout, intentamos sincronizar para evitar reintentar
            // preferencias ya aprobadas/rechazadas que terminan en error en la app de MP.
            when (val sync = pagosRepository.sincronizarPago(
                pagoId = currentPending.id,
                paymentId = currentPending.mpPaymentId?.toString()
            )) {
                is ApiResult.Success -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.PENDING_PAYMENT_SYNC_SUCCESS,
                            mapOf(
                                "pago_id" to currentPending.id.toString(),
                                "estado" to (sync.data.estado ?: "")
                            )
                        )
                    )
                    if (isPendingLike(sync.data.estado)) {
                        startPendingPolling()
                    }
                }
                is ApiResult.Error -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.PENDING_PAYMENT_SYNC_ERROR,
                            mapOf("pago_id" to currentPending.id.toString(), "message" to sync.message)
                        )
                    )
                }
                else -> Unit
            }

            loadPagoPendiente()
            val pending = _pendingPayment.value ?: currentPending
            if (!isBlockingPending(pending)) {
                _infoMessage.emit("El pago ya no está pendiente. Refrescamos tu suscripción.")
                loadSuscripcion()
                loadPagos()
                return@launch
            }

            val pendingUrl = buildPendingCheckoutUrl(pending)
            if (pendingUrl.isNullOrBlank()) {
                analytics.track(
                    AnalyticsEvent(
                        PaymentsAnalytics.PENDING_CHECKOUT_CONTINUE_ERROR,
                        mapOf("pago_id" to pending.id.toString(), "reason" to "missing_preference")
                    )
                )
                _errorMessage.emit("No se encontró el link del checkout pendiente. Intentá crear uno nuevo o sincronizar el pago.")
                return@launch
            }
            _infoMessage.emit("Continuando tu checkout pendiente...")
            _checkoutUrl.emit(pendingUrl)
        }
    }

    fun syncPendingPaymentStatus() {
        val pending = _pendingPayment.value ?: return
        viewModelScope.launch {
            analytics.track(
                AnalyticsEvent(
                    PaymentsAnalytics.PENDING_PAYMENT_SYNC_TAP,
                    mapOf("pago_id" to pending.id.toString())
                )
            )
            when (val result = pagosRepository.sincronizarPago(pagoId = pending.id, paymentId = pending.mpPaymentId?.toString())) {
                is ApiResult.Success -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.PENDING_PAYMENT_SYNC_SUCCESS,
                            mapOf(
                                "pago_id" to pending.id.toString(),
                                "estado" to (result.data.estado ?: "")
                            )
                        )
                    )
                    _infoMessage.emit("Estado actualizado: ${result.data.estado ?: "sin cambios"}")
                    refreshAfterCheckout()
                    if (isPendingLike(result.data.estado)) {
                        startPendingPolling()
                    }
                }
                is ApiResult.Error -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.PENDING_PAYMENT_SYNC_ERROR,
                            mapOf("pago_id" to pending.id.toString(), "message" to result.message)
                        )
                    )
                    _errorMessage.emit(result.message)
                }
                else -> Unit
            }
        }
    }

    fun cancelPendingPayment() {
        val paymentId = _pendingPayment.value?.id ?: return
        viewModelScope.launch {
            analytics.track(
                AnalyticsEvent(
                    PaymentsAnalytics.PENDING_PAYMENT_CANCEL_TAP,
                    mapOf("pago_id" to paymentId.toString())
                )
            )
            when (val result = pagosRepository.cancelarPagoPendiente(paymentId)) {
                is ApiResult.Success -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.PENDING_PAYMENT_CANCEL_SUCCESS,
                            mapOf("pago_id" to paymentId.toString())
                        )
                    )
                    _infoMessage.emit("Pago pendiente cancelado.")
                    clearLastCheckoutPagoIdIfMatches(paymentId)
                    refreshAfterCheckout()
                }
                is ApiResult.Error -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.PENDING_PAYMENT_CANCEL_ERROR,
                            mapOf("pago_id" to paymentId.toString(), "message" to result.message)
                        )
                    )
                    _errorMessage.emit(result.message)
                }
                else -> Unit
            }
        }
    }

    fun cancelPendingAutoRenewal() {
        val paymentId = _pendingPayment.value?.id ?: return
        viewModelScope.launch {
            analytics.track(
                AnalyticsEvent(
                    PaymentsAnalytics.PENDING_AUTO_RENEW_CANCEL_TAP,
                    mapOf("pago_id" to paymentId.toString())
                )
            )
            Log.d(TAG, "Cancel auto-renew requested -> pagoId=$paymentId")
            when (val result = pagosRepository.cancelarAutoRenovacion(paymentId)) {
                is ApiResult.Success -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.PENDING_AUTO_RENEW_CANCEL_SUCCESS,
                            mapOf("pago_id" to paymentId.toString())
                        )
                    )
                    _infoMessage.emit("Auto-renovación cancelada correctamente.")
                    refreshAfterCheckout()
                }
                is ApiResult.Error -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.PENDING_AUTO_RENEW_CANCEL_ERROR,
                            mapOf("pago_id" to paymentId.toString(), "message" to result.message)
                        )
                    )
                    _errorMessage.emit(result.message)
                }
                else -> Unit
            }
        }
    }

    fun onCheckoutReturn(
        statusRaw: String?,
        collectionStatusRaw: String?,
        paymentIdRaw: String?
    ) {
        viewModelScope.launch {
            val status = (collectionStatusRaw ?: statusRaw).orEmpty().trim().uppercase()
            analytics.track(
                AnalyticsEvent(
                    PaymentsAnalytics.CHECKOUT_DEEPLINK_RETURN,
                    mapOf(
                        "status" to status,
                        "payment_id" to paymentIdRaw.orEmpty()
                    )
                )
            )
            val message = when {
                status.contains("APPROVED") || status.contains("SUCCESS") ->
                    "Pago acreditado. Actualizando tu suscripción..."
                status.contains("PENDING") || status.contains("IN_PROCESS") ->
                    "Pago pendiente de confirmación. Lo vamos a sincronizar en breve."
                status.contains("REJECT") || status.contains("FAIL") ->
                    "El pago fue rechazado o cancelado."
                else ->
                    "Volviste desde el checkout. Verificando estado del pago..."
            }
            Log.d(TAG, "Checkout return -> status=$status paymentId=$paymentIdRaw")
            _infoMessage.emit(message)
            refreshAfterCheckout()
            if (status.contains("PENDING") || status.contains("IN_PROCESS")) {
                startPendingPolling()
            }
        }
    }

    fun refreshAfterCheckout() {
        viewModelScope.launch {
            analytics.track(AnalyticsEvent(PaymentsAnalytics.CHECKOUT_REFRESH_AFTER_RETURN))
            loadSuscripcion()
            loadPagoPendiente()
            loadPagos()
            lastPassiveRefreshAt = SystemClock.elapsedRealtime()
            maybeStartPendingPolling()
        }
    }

    fun refreshOnScreenResume(minIntervalMs: Long = 2500L) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastPassiveRefreshAt < minIntervalMs) return
        viewModelScope.launch {
            loadSuscripcion()
            loadPagoPendiente()
            loadPagos()
            lastPassiveRefreshAt = SystemClock.elapsedRealtime()
            maybeStartPendingPolling()
        }
    }

    fun trackCheckoutOpenBrowserSuccess() {
        analytics.track(AnalyticsEvent(PaymentsAnalytics.CHECKOUT_OPEN_BROWSER))
    }

    fun trackCheckoutOpenBrowserError(message: String) {
        analytics.track(
            AnalyticsEvent(
                PaymentsAnalytics.CHECKOUT_OPEN_BROWSER_ERROR,
                mapOf("message" to message)
            )
        )
    }

    fun trackCheckoutOpenMpAppSuccess() {
        analytics.track(AnalyticsEvent(PaymentsAnalytics.CHECKOUT_OPEN_MP_APP))
    }

    fun trackCheckoutOpenMpAppUnavailable() {
        analytics.track(AnalyticsEvent(PaymentsAnalytics.CHECKOUT_OPEN_MP_APP_UNAVAILABLE))
    }

    fun cancelCurrentPlanAutoRenewal() {
        val currentSubscription = _suscripcion.value
        val currentCode = currentSubscription?.plan?.codigo?.trim()?.uppercase()
        if (currentCode == "FREE") {
            viewModelScope.launch { _infoMessage.emit("Tu plan gratis no tiene auto-renovación.") }
            return
        }
        val preferredPagoId = _pagos.value
            .sortedByDescending { it.createdAt.orEmpty() }
            .firstOrNull { it.autoRenovar == true && !isPaymentFinalState(it.estadoInterno) }
            ?.id
            ?: _pagos.value
                .sortedByDescending { it.createdAt.orEmpty() }
                .firstOrNull { it.autoRenovar == true }
                ?.id
            ?: _pendingPayment.value?.id

        if (preferredPagoId == null) {
            viewModelScope.launch { _errorMessage.emit("No encontramos un pago activo para cancelar la auto-renovación.") }
            return
        }

        viewModelScope.launch {
            when (val result = pagosRepository.cancelarAutoRenovacion(preferredPagoId)) {
                is ApiResult.Success -> {
                    val planName = currentSubscription?.plan?.nombre?.ifBlank { "actual" } ?: "actual"
                    _infoMessage.emit("Auto-renovación cancelada. Tu $planName sigue activo hasta el próximo vencimiento.")
                    refreshAfterCheckout()
                }
                is ApiResult.Error -> _errorMessage.emit(result.message)
                else -> Unit
            }
        }
    }

    fun reactivateCurrentPlanAutoRenewal() {
        val currentSubscription = _suscripcion.value
        val currentCode = currentSubscription?.plan?.codigo?.trim()?.uppercase()
        if (currentCode == "FREE") {
            viewModelScope.launch { _infoMessage.emit("Tu plan gratis no tiene renovación automática.") }
            return
        }
        val preferredPagoId = _pagos.value
            .sortedByDescending { it.createdAt.orEmpty() }
            .firstOrNull {
                matchesCurrentSubscriptionPlan(it, currentSubscription) &&
                    isApprovedPayment(it.estadoInterno) &&
                    it.autoRenovar == false
            }
            ?.id
            ?: _pendingPayment.value?.takeIf {
                it.autoRenovar == false &&
                    matchesCurrentSubscriptionPlan(it, currentSubscription)
            }?.id

        if (preferredPagoId == null) {
            viewModelScope.launch {
                _errorMessage.emit("No encontramos un pago elegible para reactivar la renovación automática.")
            }
            return
        }

        viewModelScope.launch {
            analytics.track(
                AnalyticsEvent(
                    PaymentsAnalytics.AUTO_RENEW_REACTIVATE_TAP,
                    mapOf("pago_id" to preferredPagoId.toString(), "context" to "current_plan")
                )
            )
            when (val result = pagosRepository.reactivarAutoRenovacion(preferredPagoId)) {
                is ApiResult.Success -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.AUTO_RENEW_REACTIVATE_SUCCESS,
                            mapOf("pago_id" to preferredPagoId.toString())
                        )
                    )
                    val planName = currentSubscription?.plan?.nombre?.ifBlank { "plan" } ?: "plan"
                    _infoMessage.emit("Renovación automática reactivada para tu $planName.")
                    refreshAfterCheckout()
                }
                is ApiResult.Error -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.AUTO_RENEW_REACTIVATE_ERROR,
                            mapOf("pago_id" to preferredPagoId.toString(), "message" to result.message)
                        )
                    )
                    _errorMessage.emit(result.message)
                }
                else -> Unit
            }
        }
    }

    fun reactivatePendingAutoRenewal() {
        val pending = _pendingPayment.value?.takeIf { it.autoRenovar == false } ?: run {
            viewModelScope.launch {
                _errorMessage.emit("No hay un pago pendiente con renovación automática desactivada.")
            }
            return
        }
        val paymentId = pending.id
        viewModelScope.launch {
            analytics.track(
                AnalyticsEvent(
                    PaymentsAnalytics.AUTO_RENEW_REACTIVATE_TAP,
                    mapOf("pago_id" to paymentId.toString(), "context" to "pending_payment")
                )
            )
            when (val result = pagosRepository.reactivarAutoRenovacion(paymentId)) {
                is ApiResult.Success -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.AUTO_RENEW_REACTIVATE_SUCCESS,
                            mapOf("pago_id" to paymentId.toString())
                        )
                    )
                    _infoMessage.emit("Renovación automática reactivada para este pago.")
                    refreshAfterCheckout()
                }
                is ApiResult.Error -> {
                    analytics.track(
                        AnalyticsEvent(
                            PaymentsAnalytics.AUTO_RENEW_REACTIVATE_ERROR,
                            mapOf("pago_id" to paymentId.toString(), "message" to result.message)
                        )
                    )
                    _errorMessage.emit(result.message)
                }
                else -> Unit
            }
        }
    }

    private fun buildPendingCheckoutUrl(pago: PagoSuscripcionResponseDto): String? {
        val pref = pago.mpPreferenceId?.trim().orEmpty()
        if (pref.isBlank()) return null
        return if (pref.startsWith("http://", ignoreCase = true) || pref.startsWith("https://", ignoreCase = true)) {
            pref
        } else {
            "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=$pref"
        }
    }

    fun isBlockingPending(pago: PagoSuscripcionResponseDto?): Boolean {
        val status = pago?.estadoInterno?.trim()?.uppercase().orEmpty()
        if (status.isBlank()) return true
        return when {
            status.contains("PEND") || status.contains("PROCESS") || status.contains("IN_PROCESS") -> true
            status.contains("RECH") || status.contains("FAIL") || status.contains("VENC") ||
                status.contains("EXPIRE") || status.contains("CANCEL") || status.contains("REEMB") ||
                status.contains("REFUND") || status.contains("APROB") || status.contains("APPROVED") -> false
            else -> true
        }
    }

    private suspend fun loadSuscripcion() {
        when (val result = suscripcionRepository.getMiSuscripcion()) {
            is ApiResult.Success -> _suscripcion.value = result.data
            is ApiResult.Error -> _errorMessage.emit(result.message)
            else -> Unit
        }
    }

    private suspend fun loadPlanes() {
        when (val result = planesRepository.planesActivos()) {
            is ApiResult.Success -> _planes.value = result.data
            is ApiResult.Error -> _errorMessage.emit(result.message)
            else -> Unit
        }
    }

    private suspend fun loadPagos() {
        val requestSeq = ++pagosRequestSeq
        when (val result = pagosRepository.misPagos()) {
            is ApiResult.Success -> {
                if (requestSeq == pagosRequestSeq) {
                    _pagos.value = result.data
                }
            }
            is ApiResult.Error -> _errorMessage.emit(result.message)
            else -> Unit
        }
    }

    private fun isPaymentFinalState(statusRaw: String?): Boolean {
        val status = statusRaw?.trim()?.uppercase().orEmpty()
        if (status.isBlank()) return false
        return status.contains("APROB") ||
            status.contains("APPROVED") ||
            status.contains("RECH") ||
            status.contains("FAIL") ||
            status.contains("VENC") ||
            status.contains("EXPIRE") ||
            status.contains("CANCEL") ||
            status.contains("REFUND") ||
            status.contains("REEMB")
    }

    private fun isApprovedPayment(statusRaw: String?): Boolean {
        val status = statusRaw?.trim()?.uppercase().orEmpty()
        return status.contains("APROB") || status.contains("APPROVED")
    }

    private fun matchesCurrentSubscriptionPlan(
        pago: PagoSuscripcionResponseDto,
        currentSubscription: SuscripcionActualDto?
    ): Boolean {
        if (currentSubscription == null) return false
        val currentPlanId = currentSubscription.plan?.id
        val currentCode = currentSubscription.plan?.codigo?.trim()?.uppercase().orEmpty()
        val matchesPlanId = currentPlanId != null && pago.planId == currentPlanId
        val matchesPlanCode = currentCode.isNotBlank() &&
            pago.planCodigo?.trim()?.uppercase() == currentCode
        return matchesPlanId || matchesPlanCode
    }

    private suspend fun loadPagoPendiente() {
        when (val result = pagosRepository.pagoPendiente()) {
            is ApiResult.Success -> {
                _pendingPayment.value = result.data
                if (result.data != null) {
                    saveLastCheckoutPagoId(result.data.id)
                }
            }
            is ApiResult.Error -> _errorMessage.emit(result.message)
            else -> Unit
        }
    }

    private fun isPendingLike(status: String?): Boolean {
        val s = status?.trim()?.uppercase().orEmpty()
        return s.contains("PEND") || s.contains("PROCESS")
    }

    private fun maybeStartPendingPolling() {
        if (_pendingPayment.value?.let { isBlockingPending(it) } == true) {
            startPendingPolling()
        } else {
            pendingPollingJob?.cancel()
            pendingPollingJob = null
        }
    }

    private fun startPendingPolling() {
        if (pendingPollingJob?.isActive == true) return
        pendingPollingJob = viewModelScope.launch {
            repeat(6) {
                delay(15000)
                loadPagoPendiente()
                loadSuscripcion()
                loadPagos()
                if (_pendingPayment.value?.let { isBlockingPending(it) } != true) {
                    return@launch
                }
            }
        }
    }

    private fun saveLastCheckoutPagoId(pagoId: Int) {
        prefs.edit().putInt("last_checkout_pago_id", pagoId).apply()
    }

    private fun clearLastCheckoutPagoIdIfMatches(pagoId: Int) {
        val stored = prefs.getInt("last_checkout_pago_id", -1)
        if (stored == pagoId) {
            prefs.edit().remove("last_checkout_pago_id").apply()
        }
    }
}
