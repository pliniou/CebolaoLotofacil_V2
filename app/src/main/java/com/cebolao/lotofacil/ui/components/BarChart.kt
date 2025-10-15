package com.cebolao.lotofacil.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.cebolao.lotofacil.ui.theme.AppConfig
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

private val DASH_PATH_EFFECT = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
private val BAR_CORNER_RADIUS = CornerRadius(Dimen.ExtraSmallPadding.value)

@Composable
fun BarChart(
    data: ImmutableList<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = Dimen.BarChartHeight,
    maxValue: Int
) {
    val animatedProgress = remember { Animatable(0f) }
    var selectedBar by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(data) {
        selectedBar = null
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(AppConfig.Animation.LongDuration))
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val tooltipColor = MaterialTheme.colorScheme.inverseSurface
    val onTooltipColor = MaterialTheme.colorScheme.inverseOnSurface

    val density = LocalDensity.current
    val labelSmallFontSize = MaterialTheme.typography.labelSmall.fontSize
    val labelMediumFontSize = MaterialTheme.typography.labelMedium.fontSize

    val textPaint = remember(density, onSurfaceVariant, labelSmallFontSize) {
        Paint().apply {
            isAntiAlias = true
            textSize = with(density) { labelSmallFontSize.toPx() }
            color = onSurfaceVariant.toArgb()
            textAlign = Paint.Align.RIGHT
        }
    }
    val valuePaint = remember(density, primaryColor, labelSmallFontSize) {
        Paint().apply {
            isAntiAlias = true
            textSize = with(density) { labelSmallFontSize.toPx() }
            color = primaryColor.toArgb()
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }
    val labelPaint = remember(density, onSurfaceVariant, labelSmallFontSize) {
        Paint().apply {
            isAntiAlias = true
            textSize = with(density) { labelSmallFontSize.toPx() }
            color = onSurfaceVariant.toArgb()
            textAlign = Paint.Align.CENTER
        }
    }
    val tooltipTextPaint = remember(density, onTooltipColor, labelMediumFontSize) {
        Paint().apply {
            isAntiAlias = true
            textSize = with(density) { labelMediumFontSize.toPx() }
            color = onTooltipColor.toArgb()
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    Canvas(
        modifier = modifier
            .height(chartHeight)
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val yAxisLabelWidthPx = Dimen.BarChartYAxisLabelWidth.toPx()
                    val barSpacingPx = Dimen.ExtraSmallPadding.toPx()
                    val chartAreaWidth = size.width - yAxisLabelWidthPx
                    val totalSpacing = barSpacingPx * (data.size + 1)
                    val barWidth = (chartAreaWidth - totalSpacing).coerceAtLeast(0f) / data.size

                    val tappedBarIndex = data.indices.firstOrNull { index ->
                        val left =
                            yAxisLabelWidthPx + barSpacingPx + index * (barWidth + barSpacingPx)
                        offset.x >= left && offset.x <= left + barWidth
                    }
                    selectedBar = if (selectedBar == tappedBarIndex) null else tappedBarIndex
                }
            }
            .semantics {
                contentDescription =
                    "Gráfico de barras mostrando distribuição de dados. Toque em uma barra para ver o valor."
            }
    ) {
        val animated = animatedProgress.value
        val yAxisLabelWidthPx = Dimen.BarChartYAxisLabelWidth.toPx()
        val xAxisLabelHeightPx = Dimen.BarChartXAxisLabelHeight.toPx()
        val valueLabelHeightPx = Dimen.CardPadding.toPx()
        val chartAreaWidth = size.width - yAxisLabelWidthPx
        val chartAreaHeight = size.height - xAxisLabelHeightPx - valueLabelHeightPx

        drawGrid(
            yAxisLabelWidthPx,
            chartAreaHeight,
            valueLabelHeightPx,
            maxValue,
            textPaint,
            outlineVariant
        )

        val barSpacingPx = Dimen.ExtraSmallPadding.toPx()
        val totalSpacing = barSpacingPx * (data.size + 1)
        val barWidth = (chartAreaWidth - totalSpacing).coerceAtLeast(0f) / data.size
        val barRects = mutableListOf<Rect>()

        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value.toFloat() / maxValue) * chartAreaHeight * animated
            val left = yAxisLabelWidthPx + barSpacingPx + index * (barWidth + barSpacingPx)
            val top = valueLabelHeightPx + chartAreaHeight - barHeight
            barRects.add(
                Rect(
                    left = left,
                    top = top,
                    right = left + barWidth,
                    bottom = top + barHeight
                )
            )

            if (barHeight > 0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(primaryColor, secondaryColor)),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = BAR_CORNER_RADIUS
                )
            }

            val valueTextY = top - Dimen.ExtraSmallPadding.toPx()
            if (selectedBar != index) {
                drawContext.canvas.nativeCanvas.drawText(
                    value.toString(),
                    barRects[index].center.x,
                    valueTextY,
                    valuePaint
                )
            }

            val labelTextY = size.height - xAxisLabelHeightPx + Dimen.MediumPadding.toPx()
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(45f, barRects[index].center.x, labelTextY)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                barRects[index].center.x,
                labelTextY,
                labelPaint
            )
            drawContext.canvas.nativeCanvas.restore()
        }

        selectedBar?.let { index ->
            val barRect = barRects[index]
            val value = data[index].second
            drawTooltip(barRect, value.toString(), tooltipColor, tooltipTextPaint)
        }
    }
}

private fun DrawScope.drawTooltip(
    barRect: Rect,
    text: String,
    tooltipColor: Color,
    textPaint: Paint
) {
    val tooltipWidthPx = Dimen.BarChartTooltipWidth.toPx()
    val tooltipHeightPx = Dimen.BarChartTooltipHeight.toPx()
    val cornerRadiusPx = Dimen.ExtraSmallPadding.toPx()
    val tooltipMarginPx = Dimen.SmallPadding.toPx()

    val tooltipRect = Rect(
        left = barRect.center.x - tooltipWidthPx / 2,
        top = barRect.top - tooltipHeightPx - tooltipMarginPx,
        right = barRect.center.x + tooltipWidthPx / 2,
        bottom = barRect.top - tooltipMarginPx
    )

    drawRoundRect(
        color = tooltipColor,
        topLeft = tooltipRect.topLeft,
        size = tooltipRect.size,
        cornerRadius = CornerRadius(cornerRadiusPx)
    )

    val textY = tooltipRect.center.y - (textPaint.descent() + textPaint.ascent()) / 2
    drawContext.canvas.nativeCanvas.drawText(text, tooltipRect.center.x, textY, textPaint)
}

private fun DrawScope.drawGrid(
    yAxisLabelWidth: Float,
    chartAreaHeight: Float,
    topPadding: Float,
    maxValue: Int,
    textPaint: Paint,
    lineColor: Color
) {
    val gridLines = AppConfig.UI.BarChartGridLines
    (0..gridLines).forEach { i ->
        val y = topPadding + chartAreaHeight * (1f - i.toFloat() / gridLines)
        val value = (maxValue * i.toFloat() / gridLines).roundToInt()
        drawLine(
            color = lineColor.copy(alpha = 0.5f),
            start = Offset(yAxisLabelWidth, y),
            end = Offset(size.width, y),
            strokeWidth = Dimen.Border.Default.toPx(),
            pathEffect = DASH_PATH_EFFECT
        )
        val textY = y + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent()
        drawContext.canvas.nativeCanvas.drawText(
            value.toString(),
            yAxisLabelWidth - Dimen.ExtraSmallPadding.toPx(),
            textY,
            textPaint
        )
    }
}