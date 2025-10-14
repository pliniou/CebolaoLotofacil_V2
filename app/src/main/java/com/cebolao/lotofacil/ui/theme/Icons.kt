package com.cebolao.lotofacil.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.ui.graphics.vector.ImageVector
import com.cebolao.lotofacil.data.FilterType

/**
 * Mapeia um FilterType para seu ícone correspondente, centralizando a lógica de ícones.
 */
val FilterType.filterIcon: ImageVector
    get() = when (this) {
        FilterType.SOMA_DEZENAS -> Icons.Default.Calculate
        FilterType.PARES -> Icons.Default.Numbers
        FilterType.PRIMOS -> Icons.Default.Percent
        FilterType.MOLDURA -> Icons.Default.Grid4x4
        FilterType.RETRATO -> Icons.Outlined.ShapeLine
        FilterType.FIBONACCI -> Icons.Default.Timeline
        FilterType.MULTIPLOS_DE_3 -> Icons.Default.Functions
        FilterType.REPETIDAS_CONCURSO_ANTERIOR -> Icons.Default.Repeat
    }