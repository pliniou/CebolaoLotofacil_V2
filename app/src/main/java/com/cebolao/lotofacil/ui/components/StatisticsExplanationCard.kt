package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.theme.Dimen

@Composable
fun StatisticsExplanationCard(
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimen.MediumPadding)) {
            TitleWithIcon(
                text = stringResource(R.string.home_understanding_stats),
                icon = Icons.AutoMirrored.Outlined.HelpOutline
            )
            AppDivider()
            InfoPoint(
                title = stringResource(R.string.about_purpose_item1_title),
                description = stringResource(R.string.home_overdue_hot_numbers_desc)
            )
            InfoPoint(
                title = stringResource(R.string.about_purpose_item2_title),
                description = stringResource(R.string.home_distribution_charts_desc)
            )
        }
    }
}