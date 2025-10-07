package com.cebolao.lotofacil.ui.components

import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.cebolao.lotofacil.R

@Composable
fun FormattedText(
    text: String,
    modifier: Modifier = Modifier,
    style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
) {
    val annotated = remember(text, style) {
        // CORREÇÃO: Usa o método padrão do Android para parsear HTML
        htmlToAnnotatedString(text, style.color)
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.semantics {
            contentDescription = text.replace(Regex("<[^>]*>"), "")
        }
    )
}

/**
 * Converte uma string HTML simples para um AnnotatedString do Compose.
 * Usa HtmlCompat para robustez.
 */
private fun htmlToAnnotatedString(html: String, primaryColor: Color): AnnotatedString {
    val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    return buildAnnotatedString {
        append(spanned.toString())
        spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            when (span) {
                is StyleSpan -> when (span.style) {
                    android.graphics.Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    android.graphics.Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    android.graphics.Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
                }
                is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                is ForegroundColorSpan -> addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
            }
        }
    }
}

@Composable
fun ProbabilitiesTable(modifier: Modifier = Modifier) {
    val data = remember {
        listOf(
            "15 acertos" to "1 em 3.268.760",
            "14 acertos" to "1 em 21.792",
            "13 acertos" to "1 em 692",
            "12 acertos" to "1 em 60",
            "11 acertos" to "1 em 11"
        )
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TableHeader()
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        data.forEachIndexed { idx, (prize, prob) ->
            TableRow(prize, prob, isHighlighted = idx == 0)
            if (idx < data.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Prêmio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text("Probabilidade", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TableRow(prize: String, probability: String, isHighlighted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            prize,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
        Text(
            probability,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PurposeInfoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.about_purpose_desc_header), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.about_purpose_desc_body), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.about_purpose_desc_footer), style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        InfoListItem(icon = Icons.Default.CheckCircle, text = stringResource(R.string.about_purpose_item1_title), description = stringResource(R.string.about_purpose_item1_desc))
        InfoListItem(icon = Icons.Default.CheckCircle, text = stringResource(R.string.about_purpose_item2_title), description = stringResource(R.string.about_purpose_item2_desc))
        InfoListItem(icon = Icons.Default.CheckCircle, text = stringResource(R.string.about_purpose_item3_title), description = stringResource(R.string.about_purpose_item3_desc))
    }
}

@Composable
fun LegalInfoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.about_legal_desc_header), style = MaterialTheme.typography.titleMedium)
        InfoListItem(icon = Icons.Default.Gavel, text = stringResource(R.string.about_legal_item1))
        InfoListItem(icon = Icons.Default.Gavel, text = stringResource(R.string.about_legal_item2))
        InfoListItem(icon = Icons.Default.Gavel, text = stringResource(R.string.about_legal_item3))
        Spacer(Modifier.height(8.dp))
        FormattedText(text = stringResource(R.string.about_legal_footer), style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
    }
}

@Composable
fun PrivacyInfoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.about_privacy_desc_header), style = MaterialTheme.typography.titleMedium)
        FormattedText(text = stringResource(R.string.about_privacy_desc_body))
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        InfoListItem(icon = Icons.Default.Shield, text = stringResource(R.string.about_privacy_item1), iconTint = MaterialTheme.colorScheme.tertiary)
        InfoListItem(icon = Icons.Default.Shield, text = stringResource(R.string.about_privacy_item2), iconTint = MaterialTheme.colorScheme.tertiary)
        InfoListItem(icon = Icons.Default.Shield, text = stringResource(R.string.about_privacy_item3), iconTint = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun RulesInfoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.about_rules_desc_header), style = MaterialTheme.typography.titleMedium)
        InfoListItem(icon = Icons.AutoMirrored.Filled.Rule, text = stringResource(R.string.about_rules_item1))
        InfoListItem(icon = Icons.AutoMirrored.Filled.Rule, text = stringResource(R.string.about_rules_item2))
        InfoListItem(icon = Icons.AutoMirrored.Filled.Rule, text = stringResource(R.string.about_rules_item3))
    }
}

@Composable
fun BolaoInfoContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.about_bolao_desc_header), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.about_bolao_desc_body), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        FormattedText(text = stringResource(R.string.about_bolao_desc_footer))
    }
}

@Composable
fun InfoListItem(
    icon: ImageVector,
    text: String,
    description: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        verticalAlignment = if (description == null) Alignment.CenterVertically else Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.padding(top = if (description == null) 0.dp else 4.dp)
        )
        Column {
            // Usando FormattedText para lidar com as tags HTML
            FormattedText(text = text)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}