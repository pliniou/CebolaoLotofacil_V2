package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.AnimateOnEntry
import com.cebolao.lotofacil.ui.components.BolaoInfoContent
import com.cebolao.lotofacil.ui.components.InfoDialog
import com.cebolao.lotofacil.ui.components.LegalInfoContent
import com.cebolao.lotofacil.ui.components.PrivacyInfoContent
import com.cebolao.lotofacil.ui.components.ProbabilitiesTable
import com.cebolao.lotofacil.ui.components.PurposeInfoContent
import com.cebolao.lotofacil.ui.components.RulesInfoContent
import com.cebolao.lotofacil.viewmodels.MainViewModel

private data class InfoItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

@Composable
fun AboutScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    var dialogContent by remember { mutableStateOf<InfoItem?>(null) }
    // AGORA FUNCIONAL: Coleta o estado do tema.
    val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()

    dialogContent?.let { item ->
        InfoDialog(
            onDismissRequest = { dialogContent = null },
            dialogTitle = item.title,
            icon = item.icon
        ) { item.content() }
    }

    val items = remember {
        listOf(
            InfoItem(title = "Regras e Prêmios", subtitle = "O manual oficial da Caixa", icon = Icons.Default.Gavel, content = { RulesInfoContent() }),
            InfoItem(title = "A Realidade dos Números", subtitle = "A matemática da (falta de) sorte", icon = Icons.Default.Calculate, content = { ProbabilitiesTable() }),
            InfoItem(title = "Bolão: Mais Chances", subtitle = "Dividindo o prêmio (e a aposta)", icon = Icons.Default.Group, content = { BolaoInfoContent() }),
            InfoItem(title = "Finalidade do App", subtitle = "Para que (não) serve este app", icon = Icons.Default.Lightbulb, content = { PurposeInfoContent() }),
            InfoItem(title = "O Juridiquês", subtitle = "A parte chata, mas necessária", icon = Icons.Default.Info, content = { LegalInfoContent() }),
            InfoItem(title = "Sua Privacidade", subtitle = "Como (não) usamos seus dados", icon = Icons.Default.PrivacyTip, content = { PrivacyInfoContent() })
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StudioHero()
        }
        item {
            AnimateOnEntry(Modifier.padding(horizontal = 20.dp)) {
                ThemeSettingsCard(
                    currentTheme = themeMode,
                    // AGORA FUNCIONAL: Chama a função para alterar o tema.
                    onThemeChange = { mainViewModel.setThemeMode(it) }
                )
            }
        }
        items(items, key = { it.title }) { info ->
            AnimateOnEntry(Modifier.padding(horizontal = 20.dp)) {
                InfoCard(info) { dialogContent = info }
            }
        }
    }
}

@Composable
private fun StudioHero() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 24.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo_cebola),
            contentDescription = "Logo Cebola Studios",
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.studio_name), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.studio_slogan),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InfoCard(item: InfoItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(item.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSettingsCard(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Text("Aparência", style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentTheme == "light",
                    onClick = { onThemeChange("light") },
                    label = { Text("Claro") }
                )
                FilterChip(
                    selected = currentTheme == "dark",
                    onClick = { onThemeChange("dark") },
                    label = { Text("Escuro") }
                )
                FilterChip(
                    selected = currentTheme == "auto",
                    onClick = { onThemeChange("auto") },
                    label = { Text("Sistema") }
                )
            }
        }
    }
}