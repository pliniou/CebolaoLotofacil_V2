package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.ColorPaletteCard
import com.cebolao.lotofacil.ui.components.FormattedText
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.InfoListCard
import com.cebolao.lotofacil.ui.components.InfoPoint
import com.cebolao.lotofacil.ui.components.StandardScreenHeader
import com.cebolao.lotofacil.ui.components.StudioHero
import com.cebolao.lotofacil.ui.components.ThemeSettingsCard
import com.cebolao.lotofacil.ui.theme.AccentPalette
import com.cebolao.lotofacil.ui.theme.Padding

private data class InfoItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@Composable
private fun rememberInfoItems(): List<InfoItem> {
    return remember {
        listOf(
            InfoItem(
                title = "Finalidade do App",
                subtitle = "Para que serve o Cebolão",
                icon = Icons.Default.Lightbulb,
                content = { AboutPurposeContent() }
            ),
            InfoItem(
                title = "Regras Básicas",
                subtitle = "Como funciona a Lotofácil",
                icon = Icons.Default.Gavel,
                content = { AboutRulesContent() }
            ),
            InfoItem(
                title = "O que é um Bolão?",
                subtitle = "Aumente suas chances em grupo",
                icon = Icons.Default.Group,
                content = { AboutBolaoContent() }
            ),
            InfoItem(
                title = "Privacidade",
                subtitle = "Seus dados estão seguros",
                icon = Icons.Default.Lock,
                content = { AboutPrivacyContent() }
            ),
            InfoItem(
                title = "Avisos Legais",
                subtitle = "Isenção de responsabilidade",
                icon = Icons.Default.Policy,
                content = { AboutLegalContent() }
            )
        )
    }
}

@Composable
fun AboutScreen(
    currentTheme: String,
    currentPalette: AccentPalette,
    onThemeChange: (String) -> Unit,
    onPaletteChange: (AccentPalette) -> Unit
) {
    var dialogContent by remember { mutableStateOf<InfoItem?>(null) }

    dialogContent?.let { item ->
        InfoDialog(
            onDismissRequest = { dialogContent = null },
            dialogTitle = item.title,
            icon = item.icon
        ) {
            item.content()
        }
    }

    val items = rememberInfoItems()

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = Padding.Large * 4),
            verticalArrangement = Arrangement.spacedBy(Padding.Card)
        ) {
            item {
                StandardScreenHeader(
                    title = stringResource(id = R.string.about_title),
                    subtitle = stringResource(id = R.string.about_subtitle),
                    navigationIcon = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.about_title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            item {
                StudioHero(modifier = Modifier.padding(horizontal = Padding.Screen))
            }

            item {
                AnimateOnEntry(
                    modifier = Modifier.padding(horizontal = Padding.Screen)
                ) {
                    ThemeSettingsCard(
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange
                    )
                }
            }

            item {
                AnimateOnEntry(
                    modifier = Modifier.padding(horizontal = Padding.Screen)
                ) {
                    ColorPaletteCard(
                        currentPalette = currentPalette,
                        onPaletteChange = onPaletteChange
                    )
                }
            }

            items(items, key = { it.title }) { info ->
                InfoListItem(info = info, onClick = { dialogContent = info })
            }
        }
    }
}

@Composable
private fun InfoListItem(info: InfoItem, onClick: () -> Unit) {
    AnimateOnEntry(
        modifier = Modifier.padding(horizontal = Padding.Screen)
    ) {
        InfoListCard(
            title = info.title,
            subtitle = info.subtitle,
            icon = info.icon,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

@Composable
private fun AboutPurposeContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Medium)) {
        FormattedText(stringResource(R.string.about_purpose_desc_body))
        InfoPoint(stringResource(R.string.about_purpose_item1_title), stringResource(R.string.about_purpose_item1_desc))
        InfoPoint(stringResource(R.string.about_purpose_item2_title), stringResource(R.string.about_purpose_item2_desc))
        InfoPoint(stringResource(R.string.about_purpose_item3_title), stringResource(R.string.about_purpose_item3_desc))
        FormattedText(stringResource(R.string.about_purpose_desc_footer))
    }
}

@Composable
private fun AboutRulesContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Medium)) {
        InfoPoint("1.", stringResource(R.string.about_rules_item1))
        InfoPoint("2.", stringResource(R.string.about_rules_item2))
        InfoPoint("3.", stringResource(R.string.about_rules_item3))
    }
}

@Composable
private fun AboutBolaoContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Medium)) {
        FormattedText(stringResource(R.string.about_bolao_desc_body))
        FormattedText(stringResource(R.string.about_bolao_desc_footer))
    }
}

@Composable
private fun AboutPrivacyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Medium)) {
        FormattedText(stringResource(R.string.about_privacy_desc_body))
        InfoPoint("•", stringResource(R.string.about_privacy_item1))
        InfoPoint("•", stringResource(R.string.about_privacy_item2))
        InfoPoint("•", stringResource(R.string.about_privacy_item3))
    }
}

@Composable
private fun AboutLegalContent() {
    Column(verticalArrangement = Arrangement.spacedBy(Padding.Medium)) {
        InfoPoint("•", stringResource(R.string.about_legal_item1))
        InfoPoint("•", stringResource(R.string.about_legal_item2))
        InfoPoint("•", stringResource(R.string.about_legal_item3))
        FormattedText(stringResource(R.string.about_legal_footer))
    }
}