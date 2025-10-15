package com.cebolao.lotofacil.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.data.CheckResult
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableMap

@Composable
fun CheckResultCard(
    result: CheckResult,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(Dimen.Elevation.Level1)
        ),
        contentSpacing = Dimen.MediumPadding
    ) {
        val totalWins = result.scoreCounts.values.sum()
        ResultHeader(totalWins = totalWins, contestsChecked = result.lastCheckedContest)

        AppDivider()

        if (totalWins > 0) {
            ScoreBreakdown(result.scoreCounts)
            AppDivider()
            LastHitInfo(result)
        } else {
            NoWinsMessage()
        }
    }
}

@Composable
private fun ResultHeader(totalWins: Int, contestsChecked: Int) {
    val icon = if (totalWins > 0) Icons.Filled.Celebration else Icons.Filled.Analytics
    val color =
        if (totalWins > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(Dimen.MediumIcon)
        )
        Column {
            Text(
                text = if (totalWins > 0) stringResource(R.string.checker_results_header_wins)
                else stringResource(R.string.checker_results_header_no_wins),
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                text = stringResource(
                    R.string.checker_results_analysis_in_contests,
                    contestsChecked
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScoreBreakdown(scoreCounts: ImmutableMap<Int, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
        scoreCounts.entries.sortedByDescending { it.key }.forEach { (score, count) ->
            if (score >= 11) {
                val animated by animateIntAsState(
                    targetValue = count,
                    animationSpec = tween(600),
                    label = "scoreCount-$score"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.checker_score_breakdown_hits_format, score),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$animated ${
                            if (animated == 1) stringResource(R.string.checker_score_breakdown_times_format_one) else stringResource(
                                R.string.checker_score_breakdown_times_format_other
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun LastHitInfo(result: CheckResult) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(Dimen.SmallIcon)
        )
        Text(
            text = stringResource(
                R.string.checker_last_hit_info,
                result.lastHitContest ?: "--",
                result.lastHitScore ?: "--"
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoWinsMessage() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.MediumPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimen.SmallIcon)
        )
        Text(
            text = stringResource(R.string.checker_no_wins_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = Dimen.SmallPadding)
        )
    }
}