package com.cebolao.lotofacil.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Grid4x4
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

enum class StatisticPattern(val title: String, val icon: ImageVector) {
    SUM("Soma", Icons.Outlined.Calculate),
    EVENS("Pares", Icons.Outlined.LooksOne),
    PRIMES("Primos", Icons.Outlined.Percent),
    FRAME("Moldura", Icons.Outlined.Grid4x4),
    PORTRAIT("Miolo", Icons.Outlined.CropSquare),
    FIBONACCI("Fibonacci", Icons.Outlined.Timeline),
    MULTIPLES_OF_3("Múltiplos 3", Icons.Outlined.Numbers)
}