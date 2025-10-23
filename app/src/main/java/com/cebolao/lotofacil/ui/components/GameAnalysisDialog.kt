package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import com.cebolao.lotofacil.util.DEFAULT_PLACEHOLDER
import com.cebolao.lotofacil.viewmodels.GameAnalysisResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameAnalysisDialog(
    result: GameAnalysisResult,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.games_analysis_dialog_title), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimen.CardPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                    Text("Combinação", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(Dimen.ExtraSmallPadding),
                        maxItemsInEachRow = AppConfig.UI.NumberGridItemsPerRow
                    ) {
                        result.game.numbers.sorted().forEach {
                            NumberBall(
                                number = it,
                                size = Dimen.NumberBallDialog,
                                variant = NumberBallVariant.Lotofacil
                            )
                        }
                    }
                }

                SimpleStatsCard(stats = result.simpleStats)

                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimen.SmallPadding)) {
                        Text("Resumo de prêmios", style = MaterialTheme.typography.titleMedium)
                        AppDivider()
                        val totalPremios = result.checkResult.scoreCounts.values.sum()
                        val ultimoConcurso = result.checkResult.lastHitContest?.toString() ?: DEFAULT_PLACEHOLDER
                        val ultimoAcerto = result.checkResult.lastHitScore?.toString() ?: DEFAULT_PLACEHOLDER

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.bodyMedium)
                            Text("$totalPremios", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Último prêmio", style = MaterialTheme.typography.bodyMedium)
                            Text("#$ultimoConcurso (${ultimoAcerto} acertos)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(id = R.string.general_close))
            }
        }
    )
}