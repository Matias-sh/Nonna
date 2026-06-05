package com.cocido.nonna.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cocido.nonna.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val LATAM_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonnaDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    disallowFutureDates: Boolean = false
) {
    val resolvedPlaceholder = placeholder ?: stringResource(R.string.date_picker_placeholder)
    var showDatePicker by remember { mutableStateOf(false) }
    val initialDateMillis = remember(value) { parseDateToUtcMillis(value) }
    val todayUtcMillis = remember {
        LocalDate.now(ZoneOffset.UTC)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }

    NonnaTextField(
        value = value,
        onValueChange = {},
        label = label,
        placeholder = resolvedPlaceholder,
        readOnly = true,
        trailingIcon = Icons.Outlined.CalendarMonth,
        onClick = { showDatePicker = true },
        modifier = modifier
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis,
            yearRange = 1900..2100,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return !disallowFutureDates || utcTimeMillis <= todayUtcMillis
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onValueChange(formatUtcMillisToIsoDate(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.common_accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun parseDateToUtcMillis(value: String): Long? {
    if (value.isBlank()) return null
    val parsedDate = runCatching { LocalDate.parse(value, ISO_DATE_FORMATTER) }
        .recoverCatching { LocalDate.parse(value, LATAM_DATE_FORMATTER) }
        .getOrNull()
        ?: return null
    return parsedDate
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}

private fun formatUtcMillisToIsoDate(utcMillis: Long): String {
    val localDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
    return ISO_DATE_FORMATTER.format(localDate)
}

