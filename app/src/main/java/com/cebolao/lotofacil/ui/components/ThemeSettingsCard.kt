package com.cebolao.lotofacil.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

private data class ThemeOption(val key: String, val label: String, val icon: ImageVector)

private val THEME_OPTIONS = listOf(
    ThemeOption("light", "Claro", Icons.Default.LightMode),
    ThemeOption("dark", "Escuro", Icons.Default.DarkMode),
    ThemeOption("auto", "Sistema", Icons.Default.SettingsBrightness)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsCard(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        TitleWithIcon(text = "Tema do Aplicativo", icon = Icons.Default.SettingsBrightness)
        MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            THEME_OPTIONS.forEach { option ->
                SegmentedButton(
                    checked = currentTheme == option.key,
                    onCheckedChange = { onThemeChange(option.key) },
                    shape = MaterialTheme.shapes.medium,
                    label = { Text(option.label) },
                    icon = { Icon(imageVector = option.icon, contentDescription = null) }
                )
            }
        }
    }
}