package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.RestrictivenessCategory
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen

private object FilterStatsPanelConstants {
    const val PROBABILITY_THRESHOLD_LOW = 0.1f
    const val PROBABILITY_THRESHOLD_MEDIUM = 0.4f
}

@Composable
fun FilterStatsPanel(
    activeFilters: List<FilterState>,
    successProbability: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimen.Elevation.Level1)
        ),
        elevation = CardDefaults.cardElevation(Dimen.Elevation.Level1)
    ) {
        Column(
            modifier = Modifier.padding(Dimen.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)
        ) {
            Text("Análise dos Filtros", style = MaterialTheme.typography.titleMedium)
            FilterRestrictiveness(probability = successProbability)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = AppConfig.UI.FilterPanelOutlineAlpha))
            FilterStatistics(activeFilters)
        }
    }
}

@Composable
private fun FilterRestrictiveness(probability: Float) {
    val animatedProbability by animateFloatAsState(
        targetValue = probability,
        animationSpec = tween(AppConfig.Animation.MediumDuration),
        label = "probabilityAnimation"
    )

    val (progressColor, textColor) = when {
        animatedProbability < FilterStatsPanelConstants.PROBABILITY_THRESHOLD_LOW -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error
        animatedProbability < FilterStatsPanelConstants.PROBABILITY_THRESHOLD_MEDIUM -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary
    }

    val animatedProgressColor by animateColorAsState(
        targetValue = progressColor,
        label = "progressColor"
    )
    val animatedTextColor by animateColorAsState(targetValue = textColor, label = "textColor")

    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Chance de Sucesso", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${(animatedProbability * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = animatedTextColor
            )
        }
        LinearProgressIndicator(
            progress = { animatedProbability },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimen.ProgressBarHeight)
                .clip(MaterialTheme.shapes.small),
            color = animatedProgressColor,
            trackColor = animatedProgressColor.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun FilterStatistics(filters: List<FilterState>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
        if (filters.none { it.isEnabled }) {
            Text(
                "Nenhum filtro ativo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            filters.filter { it.isEnabled }.forEach { filter ->
                FilterStatRow(filter)
            }
        }
    }
}

@Composable
private fun FilterStatRow(filter: FilterState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(filter.type.title, style = MaterialTheme.typography.bodyMedium)
        RestrictivenessChip(filter.restrictivenessCategory)
    }
}

@Composable
private fun RestrictivenessChip(category: RestrictivenessCategory) {
    val (color, text) = when (category) {
        RestrictivenessCategory.VERY_TIGHT -> MaterialTheme.colorScheme.error to "Muito Restrito"
        RestrictivenessCategory.TIGHT -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f) to "Restrito"
        RestrictivenessCategory.MODERATE -> MaterialTheme.colorScheme.tertiary to "Moderado"
        RestrictivenessCategory.LOOSE -> MaterialTheme.colorScheme.primary to "Flexível"
        RestrictivenessCategory.VERY_LOOSE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) to "Muito Flexível"
        RestrictivenessCategory.DISABLED -> MaterialTheme.colorScheme.outline to "Desabilitado"
    }
    Surface(
        color = color.copy(alpha = AppConfig.UI.FilterPanelChipBgAlpha),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(
                horizontal = Dimen.SmallPadding,
                vertical = Dimen.ExtraSmallPadding
            )
        )
    }
}