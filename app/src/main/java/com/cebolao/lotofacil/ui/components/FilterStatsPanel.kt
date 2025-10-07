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
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.data.FilterState
import com.cebolao.lotofacil.data.RestrictivenessCategory

@Composable
fun FilterStatsPanel(
    activeFilters: List<FilterState>,
    successProbability: Float, // ATUALIZADO: Recebe a probabilidade de sucesso
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Análise dos Filtros", style = MaterialTheme.typography.titleMedium)

            // ATUALIZAÇÃO: Usa a nova métrica de probabilidade
            FilterRestrictiveness(probability = successProbability)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            FilterStatistics(activeFilters)
        }
    }
}

@Composable
private fun FilterRestrictiveness(probability: Float) {
    val animatedProbability by animateFloatAsState(
        targetValue = probability,
        animationSpec = tween(500),
        label = "probabilityAnimation"
    )

    val (progressColor, textColor) = when {
        animatedProbability < 0.1f -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error
        animatedProbability < 0.4f -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary
    }

    val animatedProgressColor by animateColorAsState(targetValue = progressColor, label = "progressColor")
    val animatedTextColor by animateColorAsState(targetValue = textColor, label = "textColor")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                .height(6.dp)
                .clip(MaterialTheme.shapes.small),
            color = animatedProgressColor,
            trackColor = animatedProgressColor.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun FilterStatistics(filters: List<FilterState>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}