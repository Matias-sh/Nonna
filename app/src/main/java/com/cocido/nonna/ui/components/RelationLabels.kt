package com.cocido.nonna.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.cocido.nonna.R

@Composable
fun relationCategoryLabel(raw: String): String {
    return when (raw) {
        "Familia" -> stringResource(R.string.relation_category_family)
        "Relaciones especiales" -> stringResource(R.string.relation_category_special)
        else -> raw
    }
}

@Composable
fun relationOptionLabel(raw: String): String {
    val resId = relationResId(raw)
    return if (resId != null) stringResource(resId) else raw
}

@Composable
fun relationValueLabel(raw: String): String {
    val resId = relationResId(raw)
    return if (resId != null) stringResource(resId) else raw
}

@StringRes
private fun relationResId(raw: String): Int? {
    return when (raw.trim()) {
        "Padre", "PADRE" -> R.string.relation_padre
        "Madre", "MADRE" -> R.string.relation_madre
        "Hijo", "HIJO" -> R.string.relation_hijo
        "Hija", "HIJA" -> R.string.relation_hija
        "Abuelo", "ABUELO" -> R.string.relation_abuelo
        "Abuela", "ABUELA" -> R.string.relation_abuela
        "Nieto", "NIETO" -> R.string.relation_nieto
        "Nieta", "NIETA" -> R.string.relation_nieta
        "Bisabuelo", "BISABUELO" -> R.string.relation_bisabuelo
        "Bisabuela", "BISABUELA" -> R.string.relation_bisabuela
        "Hermano", "HERMANO" -> R.string.relation_hermano
        "Hermana", "HERMANA" -> R.string.relation_hermana
        "Tío", "TIO" -> R.string.relation_tio
        "Tía", "TIA" -> R.string.relation_tia
        "Sobrino", "SOBRINO" -> R.string.relation_sobrino
        "Sobrina", "SOBRINA" -> R.string.relation_sobrina
        "Primo", "PRIMO" -> R.string.relation_primo
        "Prima", "PRIMA" -> R.string.relation_prima
        "Suegro", "SUEGRO" -> R.string.relation_suegro
        "Suegra", "SUEGRA" -> R.string.relation_suegra
        "Yerno", "YERNO" -> R.string.relation_yerno
        "Nuera", "NUERA" -> R.string.relation_nuera
        "Cuñado", "CUÑADO" -> R.string.relation_cunado
        "Cuñada", "CUÑADA" -> R.string.relation_cunada
        "Padrino", "PADRINO" -> R.string.relation_padrino
        "Madrina", "MADRINA" -> R.string.relation_madrina
        "Ahijado", "AHIJADO" -> R.string.relation_ahijado
        "Ahijada", "AHIJADA" -> R.string.relation_ahijada
        "Amigo", "AMIGO" -> R.string.relation_amigo
        "Amiga", "AMIGA" -> R.string.relation_amiga
        "Pareja", "PAREJA" -> R.string.relation_pareja
        "Otro", "OTRO" -> R.string.relation_otro
        else -> null
    }
}
