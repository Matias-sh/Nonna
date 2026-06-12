package com.cocido.nonna.ui.screens.profile

import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cocido.nonna.R
import com.cocido.nonna.data.remote.dto.PagoSuscripcionResponseDto
import com.cocido.nonna.data.remote.dto.PeriodicidadPago
import com.cocido.nonna.data.remote.dto.SuscripcionPlanDto
import com.cocido.nonna.ui.components.NonnaBottomFeedbackBanner
import com.cocido.nonna.ui.components.NonnaButton
import com.cocido.nonna.ui.components.NonnaButtonSize
import com.cocido.nonna.ui.components.NonnaButtonStyle
import com.cocido.nonna.ui.components.NonnaFeedbackType
import com.cocido.nonna.ui.components.PageHeader
import com.cocido.nonna.ui.components.ScreenTitleSection
import com.cocido.nonna.ui.theme.NonnaCorners
import com.cocido.nonna.ui.theme.NonnaDimens
import com.cocido.nonna.ui.viewmodel.SubscriptionCenterViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.LinkedHashMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

private const val SUBSCRIPTION_CENTER_TAG = "SubscriptionCenterUI"
private const val MERCADO_PAGO_PACKAGE = "com.mercadopago.wallet"

@Composable
fun SubscriptionCenterScreen(
    onBack: () -> Unit,
    checkoutStatus: String? = null,
    checkoutCollectionStatus: String? = null,
    checkoutPaymentId: String? = null,
    viewModel: SubscriptionCenterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isProcessingCheckout by viewModel.isProcessingCheckout.collectAsStateWithLifecycle()
    val suscripcion by viewModel.suscripcion.collectAsStateWithLifecycle()
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val planes by viewModel.planes.collectAsStateWithLifecycle()
    val pendingPayment by viewModel.pendingPayment.collectAsStateWithLifecycle()
    val pagos by viewModel.pagos.collectAsStateWithLifecycle()
    val isPendingBlocking = remember(pendingPayment) { viewModel.isBlockingPending(pendingPayment) }
    val currentPlanCode = (
        billingState?.plan?.codigo
            ?: suscripcion?.plan?.codigo
        )?.trim()?.uppercase(Locale.getDefault())
    val currentPlanName = (
        billingState?.plan?.nombre?.ifBlank { null }
            ?: suscripcion?.plan?.nombre
        )?.ifBlank { "plan actual" } ?: "plan actual"
    val historyPayments = remember(pagos, pendingPayment) {
        val mergedById = LinkedHashMap<Int, PagoSuscripcionResponseDto>()
        pendingPayment?.let { pending -> mergedById[pending.id] = pending }
        // mis-pagos es la fuente de verdad para estado final (aprobado/cancelado/etc.).
        // Si coincide el ID con pendiente, este valor debe prevalecer.
        pagos.forEach { pago -> mergedById[pago.id] = pago }
        mergedById.values.sortedByDescending { it.createdAt.orEmpty() }
    }
    val latestCurrentPlanPayment = remember(historyPayments, suscripcion) {
        val currentPlanId = suscripcion?.plan?.id
        val normalizedCurrentCode = currentPlanCode.orEmpty()
        historyPayments
            .filter { pago ->
                val matchesPlanId = currentPlanId != null && pago.planId == currentPlanId
                val matchesPlanCode = !normalizedCurrentCode.isBlank() &&
                    pago.planCodigo?.trim()?.uppercase(Locale.getDefault()) == normalizedCurrentCode
                matchesPlanId || matchesPlanCode
            }
            .firstOrNull()
    }
    val autoRenewEnabled = billingState?.autoRenovar ?: latestCurrentPlanPayment?.autoRenovar
    val canCancelCurrentPlan = currentPlanCode != null && currentPlanCode != "FREE" && autoRenewEnabled != false
    val canResumeAutoRenewal =
        currentPlanCode != null && currentPlanCode != "FREE" && autoRenewEnabled == false
    val subscriptionEndsAt = suscripcion?.fechaFin

    var feedbackVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf(NonnaFeedbackType.Error) }
    var showCancelPlanDialog by remember { mutableStateOf(false) }
    var showResumeRenewDialog by remember { mutableStateOf(false) }
    var showResumePendingRenewDialog by remember { mutableStateOf(false) }

    val checkoutPlans = remember(planes) {
        planes.filter { it.codigo?.trim()?.uppercase(Locale.getDefault()) != "FREE" }
    }

    LaunchedEffect(Unit) { viewModel.load() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshOnScreenResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(checkoutStatus, checkoutCollectionStatus, checkoutPaymentId) {
        if (
            !checkoutStatus.isNullOrBlank() ||
            !checkoutCollectionStatus.isNullOrBlank() ||
            !checkoutPaymentId.isNullOrBlank()
        ) {
            Log.d(
                SUBSCRIPTION_CENTER_TAG,
                "DeepLink return -> status=$checkoutStatus collectionStatus=$checkoutCollectionStatus paymentId=$checkoutPaymentId"
            )
            viewModel.onCheckoutReturn(
                statusRaw = checkoutStatus,
                collectionStatusRaw = checkoutCollectionStatus,
                paymentIdRaw = checkoutPaymentId
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { msg ->
            feedbackMessage = msg
            feedbackType = if (
                msg.contains("cancelada", ignoreCase = true) ||
                    msg.contains("reactiva", ignoreCase = true)
            ) {
                NonnaFeedbackType.Success
            } else {
                NonnaFeedbackType.Error
            }
            feedbackVisible = true
            delay(2300)
            feedbackVisible = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.infoMessage.collect { msg ->
            feedbackMessage = msg
            feedbackType = NonnaFeedbackType.Info
            feedbackVisible = true
            delay(2300)
            feedbackVisible = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkoutUrl.collect { url ->
            Log.d(SUBSCRIPTION_CENTER_TAG, "Opening checkout URL")
            val uri = Uri.parse(url)
            val openedInMp = runCatching {
                val mpIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    `package` = MERCADO_PAGO_PACKAGE
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(mpIntent)
                true
            }.getOrElse { err ->
                if (err is ActivityNotFoundException) {
                    viewModel.trackCheckoutOpenMpAppUnavailable()
                } else {
                    Log.w(SUBSCRIPTION_CENTER_TAG, "MP app open failed: ${err.message}")
                }
                false
            }
            if (openedInMp) {
                viewModel.trackCheckoutOpenMpAppSuccess()
            } else {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, uri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                    viewModel.trackCheckoutOpenBrowserSuccess()
                }.onFailure {
                    viewModel.trackCheckoutOpenBrowserError(it.message ?: "open_browser_failed")
                    feedbackMessage = context.getString(R.string.subscription_checkout_open_error)
                    feedbackType = NonnaFeedbackType.Error
                    feedbackVisible = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageHeader(
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NonnaDimens.screenPaddingHorizontal)
        ) {
            ScreenTitleSection(
                title = stringResource(R.string.subscription_center_title),
                subtitle = stringResource(R.string.subscription_center_subtitle)
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            CurrentSubscriptionCard(
                planName = suscripcion?.plan?.nombre,
                status = suscripcion?.estado,
                autoRenewEnabled = autoRenewEnabled,
                subscriptionEndsAt = subscriptionEndsAt,
                canCancelPlan = canCancelCurrentPlan,
                onCancelPlan = { showCancelPlanDialog = true },
                canResumeAutoRenew = canResumeAutoRenewal,
                onResumeAutoRenew = { showResumeRenewDialog = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PendingPaymentCard(
                pendingPayment = pendingPayment,
                isBlockingPending = isPendingBlocking,
                onContinuePending = { viewModel.continuePendingCheckout() },
                onCancelPending = { viewModel.cancelPendingPayment() },
                onCancelAutoRenew = { viewModel.cancelPendingAutoRenewal() },
                onResumeAutoRenewRequest = { showResumePendingRenewDialog = true }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PlansCheckoutCard(
                plans = checkoutPlans,
                isProcessingCheckout = isProcessingCheckout,
                currentPlanId = suscripcion?.plan?.id,
                hasPendingPayment = pendingPayment != null,
                isPendingBlocking = isPendingBlocking,
                onContinuePending = { viewModel.continuePendingCheckout() },
                onCheckoutMonthly = { plan ->
                    viewModel.startCheckout(plan, periodicidad = PeriodicidadPago.Mensual)
                },
                onCheckoutYearly = { plan ->
                    viewModel.startCheckout(plan, periodicidad = PeriodicidadPago.Anual)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PaymentsHistoryCard(historyPayments)
            Spacer(modifier = Modifier.height(100.dp))
        }
        NonnaBottomFeedbackBanner(
            visible = feedbackVisible && feedbackMessage.isNotBlank(),
            message = feedbackMessage,
            type = feedbackType
        )
    }

    if (showCancelPlanDialog) {
        AlertDialog(
            onDismissRequest = { showCancelPlanDialog = false },
            title = { Text(text = stringResource(R.string.subscription_cancel_confirm_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.subscription_cancel_confirm_message,
                        currentPlanName
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelPlanDialog = false
                        viewModel.cancelCurrentPlanAutoRenewal()
                    }
                ) {
                    Text(text = stringResource(R.string.subscription_cancel_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelPlanDialog = false }) {
                    Text(text = stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showResumeRenewDialog) {
        AlertDialog(
            onDismissRequest = { showResumeRenewDialog = false },
            title = { Text(text = stringResource(R.string.subscription_resume_auto_renew_confirm_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.subscription_resume_auto_renew_confirm_message,
                        currentPlanName
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResumeRenewDialog = false
                        viewModel.reactivateCurrentPlanAutoRenewal()
                    }
                ) {
                    Text(text = stringResource(R.string.subscription_resume_auto_renew_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResumeRenewDialog = false }) {
                    Text(text = stringResource(R.string.common_cancel))
                }
            }
        )
    }

    val pendingForResumeDialog = pendingPayment
    if (
        showResumePendingRenewDialog &&
            pendingForResumeDialog != null &&
            pendingForResumeDialog.autoRenovar == false
    ) {
        val pendingPlanLabel =
            pendingForResumeDialog.planNombre?.ifBlank { currentPlanName } ?: currentPlanName
        AlertDialog(
            onDismissRequest = { showResumePendingRenewDialog = false },
            title = { Text(text = stringResource(R.string.subscription_resume_auto_renew_confirm_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.subscription_resume_auto_renew_confirm_message,
                        pendingPlanLabel
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResumePendingRenewDialog = false
                        viewModel.reactivatePendingAutoRenewal()
                    }
                ) {
                    Text(text = stringResource(R.string.subscription_resume_auto_renew_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResumePendingRenewDialog = false }) {
                    Text(text = stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun CurrentSubscriptionCard(
    planName: String?,
    status: String?,
    autoRenewEnabled: Boolean?,
    subscriptionEndsAt: String?,
    canCancelPlan: Boolean,
    onCancelPlan: () -> Unit,
    canResumeAutoRenew: Boolean,
    onResumeAutoRenew: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)) {
            Text(
                text = stringResource(R.string.subscription_current_plan_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = planName?.ifBlank { "—" } ?: "—",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            status?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.subscription_status_label) + ": $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (autoRenewEnabled) {
                    true -> stringResource(R.string.subscription_auto_renew_on)
                    false -> stringResource(R.string.subscription_auto_renew_off)
                    null -> stringResource(R.string.subscription_auto_renew_unknown)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            subscriptionEndsAt
                ?.takeIf { it.isNotBlank() }
                ?.let { rawDate ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.subscription_active_until,
                            formatDateTime(rawDate)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            if (canResumeAutoRenew) {
                Spacer(modifier = Modifier.height(12.dp))
                NonnaButton(
                    text = stringResource(R.string.subscription_resume_auto_renew_cta),
                    onClick = onResumeAutoRenew,
                    style = NonnaButtonStyle.Primary
                )
            }
            if (canCancelPlan) {
                Spacer(modifier = Modifier.height(12.dp))
                NonnaButton(
                    text = stringResource(R.string.subscription_cancel_plan_cta),
                    onClick = onCancelPlan,
                    style = NonnaButtonStyle.Destructive
                )
            }
        }
    }
}

@Composable
private fun PendingPaymentCard(
    pendingPayment: PagoSuscripcionResponseDto?,
    isBlockingPending: Boolean,
    onContinuePending: () -> Unit,
    onCancelPending: () -> Unit,
    onCancelAutoRenew: () -> Unit,
    onResumeAutoRenewRequest: () -> Unit
) {
    if (pendingPayment == null) return
    val canManageAutoRenew = !isBlockingPending &&
        pendingPayment.planCodigo?.trim()?.uppercase(Locale.getDefault()) != "FREE"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)) {
            Text(
                text = stringResource(R.string.subscription_pending_payment_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pendingPayment.planNombre.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatMoney(pendingPayment.monto, pendingPayment.moneda),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PaymentStatusChip(statusRaw = pendingPayment.estadoInterno)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatDateTime(pendingPayment.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (isBlockingPending) {
                NonnaButton(
                    text = stringResource(R.string.subscription_continue_pending_payment),
                    onClick = onContinuePending,
                    style = NonnaButtonStyle.Primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                NonnaButton(
                    text = stringResource(R.string.subscription_cancel_pending_payment),
                    onClick = onCancelPending,
                    style = NonnaButtonStyle.Destructive
                )
            } else {
                Text(
                    text = stringResource(R.string.subscription_pending_not_blocking_new_checkout),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (canManageAutoRenew) {
                when (pendingPayment.autoRenovar) {
                    true -> {
                        NonnaButton(
                            text = stringResource(R.string.subscription_cancel_auto_renew),
                            onClick = onCancelAutoRenew,
                            style = NonnaButtonStyle.Outline
                        )
                    }
                    false -> {
                        NonnaButton(
                            text = stringResource(R.string.subscription_resume_auto_renew_cta),
                            onClick = onResumeAutoRenewRequest,
                            style = NonnaButtonStyle.Primary
                        )
                    }
                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun PlansCheckoutCard(
    plans: List<SuscripcionPlanDto>,
    isProcessingCheckout: Boolean,
    currentPlanId: Int?,
    hasPendingPayment: Boolean,
    isPendingBlocking: Boolean,
    onContinuePending: () -> Unit,
    onCheckoutMonthly: (SuscripcionPlanDto) -> Unit,
    onCheckoutYearly: (SuscripcionPlanDto) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)) {
            Text(
                text = stringResource(R.string.subscription_checkout_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.subscription_checkout_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (plans.isEmpty()) {
                Text(
                    text = stringResource(R.string.subscription_checkout_no_paid_plans),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                if (hasPendingPayment && isPendingBlocking) {
                    Text(
                        text = stringResource(R.string.subscription_pending_checkout_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NonnaButton(
                        text = stringResource(R.string.subscription_continue_pending_payment),
                        onClick = onContinuePending,
                        style = NonnaButtonStyle.Outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                plans.forEachIndexed { index, plan ->
                    val isCurrentPlan = currentPlanId != null && currentPlanId == plan.id
                    val canStartCheckout =
                        !isCurrentPlan && !isProcessingCheckout && !(hasPendingPayment && isPendingBlocking)
                    PlanCheckoutRow(
                        plan = plan,
                        enabled = canStartCheckout,
                        isCurrentPlan = isCurrentPlan,
                        blockedByPending = hasPendingPayment,
                        onMonthly = { onCheckoutMonthly(plan) },
                        onYearly = { onCheckoutYearly(plan) }
                    )
                    if (index < plans.lastIndex) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCheckoutRow(
    plan: SuscripcionPlanDto,
    enabled: Boolean,
    isCurrentPlan: Boolean,
    blockedByPending: Boolean,
    onMonthly: () -> Unit,
    onYearly: () -> Unit
) {
    Column {
        Text(
            text = plan.nombre?.ifBlank { plan.codigo.orEmpty() }.orEmpty(),
            style = MaterialTheme.typography.titleSmall
        )
        if (isCurrentPlan) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.subscription_current_plan_badge),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        plan.descripcion?.takeIf { it.isNotBlank() }?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val monthlyPrice = formatMoneyCompact(plan.precioMensual, plan.moneda)
            val yearlyAmount = plan.precioAnual ?: plan.precioMensual?.times(12)
            val yearlyPrice = formatMoneyCompact(yearlyAmount, plan.moneda)
            NonnaButton(
                text = stringResource(
                    R.string.subscription_checkout_monthly_short_cta,
                    monthlyPrice
                ),
                onClick = onMonthly,
                enabled = enabled,
                style = NonnaButtonStyle.Primary,
                size = NonnaButtonSize.Small,
                modifier = Modifier.weight(1f)
            )
            NonnaButton(
                text = stringResource(
                    R.string.subscription_checkout_yearly_short_cta,
                    yearlyPrice
                ),
                onClick = onYearly,
                enabled = enabled,
                style = NonnaButtonStyle.Outline,
                size = NonnaButtonSize.Small,
                modifier = Modifier.weight(1f)
            )
        }
        if (blockedByPending && !isCurrentPlan && !enabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.subscription_pending_blocks_checkout),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaymentsHistoryCard(pagos: List<PagoSuscripcionResponseDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = NonnaCorners.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(NonnaDimens.cardPaddingLarge)) {
            Text(
                text = stringResource(R.string.subscription_payments_history_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (pagos.isEmpty()) {
                Text(
                    text = stringResource(R.string.subscription_payments_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                pagos.take(8).forEachIndexed { index, pago ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pago.planNombre ?: "Plan",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatDateTime(pago.createdAt) + " · " + formatMoney(pago.monto, pago.moneda),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PaymentStatusChip(statusRaw = pago.estadoInterno)
                    }
                    if (index < pagos.take(8).lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusChip(statusRaw: String?) {
    val status = statusRaw.orEmpty().trim().uppercase(Locale.getDefault())
    val (bg, textColor, label) = when {
        status.contains("APROB") || status.contains("APPROVED") ->
            Triple(Color(0xFFDFF4E5), Color(0xFF1B5E20), stringResource(R.string.payment_status_approved))
        status.contains("PEND") || status.contains("PROCESS") ->
            Triple(Color(0xFFFFF1D6), Color(0xFF7A4B00), stringResource(R.string.payment_status_pending))
        status.contains("RECH") || status.contains("FAIL") ->
            Triple(Color(0xFFFCE1E1), Color(0xFF8B1E1E), stringResource(R.string.payment_status_rejected))
        status.contains("CANCEL") ->
            Triple(Color(0xFFECECEC), Color(0xFF555555), stringResource(R.string.payment_status_cancelled))
        status.contains("VENC") || status.contains("EXPIRE") ->
            Triple(Color(0xFFFFE5CC), Color(0xFF8A4500), stringResource(R.string.payment_status_expired))
        status.contains("REEMB") || status.contains("REFUND") ->
            Triple(Color(0xFFE4F0FF), Color(0xFF0D47A1), stringResource(R.string.payment_status_refunded))
        else ->
            Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, statusRaw.orEmpty())
    }
    Surface(shape = NonnaCorners.Full, color = bg) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

private fun formatDateTime(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return "—"
    return runCatching {
        parseIsoDateTime(value)
    }.getOrDefault(value)
}

private fun parseIsoDateTime(value: String): String {
    val output = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("es", "AR"))
        .withZone(ZoneId.systemDefault())

    return try {
        output.format(Instant.parse(value))
    } catch (_: Exception) {
        try {
            output.format(OffsetDateTime.parse(value).toInstant())
        } catch (_: Exception) {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            input.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val parsed = input.parse(value) ?: return value
            val fallback = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR"))
            fallback.format(parsed)
        }
    }
}

private fun formatMoney(amount: Double?, currency: String?): String {
    if (amount == null) return "—"
    val locale = Locale("es", "AR")
    val fmt = NumberFormat.getCurrencyInstance(locale)
    val code = currency?.trim()?.uppercase(Locale.getDefault())
    if (!code.isNullOrBlank()) {
        runCatching { fmt.currency = java.util.Currency.getInstance(code) }
    }
    return fmt.format(amount)
}

private fun formatMoneyCompact(amount: Double?, currency: String?): String {
    if (amount == null) return "—"
    val locale = Locale("es", "AR")
    val fmt = NumberFormat.getCurrencyInstance(locale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    val code = currency?.trim()?.uppercase(Locale.getDefault())
    if (!code.isNullOrBlank()) {
        runCatching { fmt.currency = java.util.Currency.getInstance(code) }
    }
    return fmt.format(amount)
}
