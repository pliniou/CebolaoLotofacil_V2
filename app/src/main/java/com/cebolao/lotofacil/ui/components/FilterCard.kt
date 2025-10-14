package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.FilterType
import com.cebolao.lotofacil.ui.theme.Padding
import com.cebolao.lotofacil.ui.theme.Sizes
import com.cebolao.lotofacil.ui.theme.filterIcon

private val ELEVATION_ENABLED = 3.dp
private val ELEVATION_DISABLED = 1.dp
private val BORDER_WIDTH = 1.dp
private const val BORDER_ANIMATION_DURATION_MS = 300

@Composable
fun FilterCard(
    modifier: Modifier = Modifier,
    filterState: FilterState,
    onEnabledChange: (Boolean) -> Unit,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onInfoClick: () -> Unit,
    lastDrawNumbers: Set<Int>? = null
) {
    val haptic = LocalHapticFeedback.current
    val requiresData = filterState.type == FilterType.REPETIDAS_CONCURSO_ANTERIOR
    val dataAvailable = !requiresData || lastDrawNumbers != null
    val enabled = filterState.isEnabled && dataAvailable

    val elevation by animateDpAsState(
        if (enabled) ELEVATION_ENABLED else ELEVATION_DISABLED,
        spring(stiffness = Spring.StiffnessMedium),
        label = "elevation"
    )
    val borderColor by animateColorAsState(
        if (enabled) MaterialTheme.colorScheme.primary else Color.Transparent,
        tween(BORDER_ANIMATION_DURATION_MS),
        label = "borderColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(elevation),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(elevation)),
        border = BorderStroke(BORDER_WIDTH, borderColor)
    ) {
        Column(modifier = Modifier.padding(horizontal = Padding.Card, vertical = Padding.Medium)) {
            FilterHeader(
                filterState = filterState,
                dataAvailable = dataAvailable,
                onInfoClick = onInfoClick,
                onToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEnabledChange(!filterState.isEnabled)
                }
            )
            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
            ) {
                FilterContent(
                    filterState = filterState,
                    onRangeChange = onRangeChange,
                    onRangeFinished = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                )
            }
        }
    }
}

@Composable
private fun FilterHeader(
    filterState: FilterState,
    dataAvailable: Boolean,
    onInfoClick: () -> Unit,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Padding.Small)
    ) {
        Icon(
            imageVector = filterState.type.filterIcon,
            contentDescription = filterState.type.title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Sizes.IconMedium)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(filterState.type.title, style = MaterialTheme.typography.titleSmall)
            if (!dataAvailable) {
                Text(
                    "Dados indisponíveis",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        IconButton(onClick = onInfoClick) {
            Icon(Icons.Outlined.Info, contentDescription = "Mais informações sobre o filtro ${filterState.type.title}")
        }
        Switch(
            checked = filterState.isEnabled,
            onCheckedChange = { onToggle() },
            enabled = dataAvailable
        )
    }
}

@Composable
private fun FilterContent(
    filterState: FilterState,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onRangeFinished: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = Padding.Medium),
        verticalArrangement = Arrangement.spacedBy(Padding.ExtraSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ValueIndicator("Mínimo", filterState.selectedRange.start.toInt())
            ValueIndicator("Máximo", filterState.selectedRange.endInclusive.toInt(), Alignment.End)
        }
        RangeSlider(
            value = filterState.selectedRange,
            onValueChange = onRangeChange,
            valueRange = filterState.type.fullRange,
            steps = (filterState.type.fullRange.endInclusive - filterState.type.fullRange.start).toInt() - 1,
            onValueChangeFinished = onRangeFinished
        )
    }
}

@Composable
private fun ValueIndicator(label: String, value: Int, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}