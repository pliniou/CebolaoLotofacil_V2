package com.cebolao.lotofacil.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Locale

/**
 * Retorna uma instância memorizada de [NumberFormat] para a formatação de moeda no padrão pt-BR.
 * O uso de `remember` evita a recriação do objeto em cada recomposição.
 */
@Composable
fun rememberCurrencyFormatter(): NumberFormat {
    return remember {
        NumberFormat.getCurrencyInstance(Locale(LOCALE_LANGUAGE, LOCALE_COUNTRY))
    }
}