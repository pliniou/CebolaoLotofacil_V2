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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

@Composable
fun BarChart(
    data: ImmutableList<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 200.dp,
    maxValue: Int
) {
    val animatedProgress = remember { Animatable(0f) }
    var selectedBar by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(data) {
        selectedBar = null // Reseta a seleção quando os dados mudam
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(700))
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val tooltipColor = MaterialTheme.colorScheme.inverseSurface
    val onTooltipColor = MaterialTheme.colorScheme.inverseOnSurface

    val density = LocalDensity.current
    val textPaint = remember(density, onSurfaceVariant) {
        Paint().apply {
            isAntiAlias = true
            textSize = density.run { 10.sp.toPx() }
            color = onSurfaceVariant.toArgb()
            textAlign = Paint.Align.RIGHT
        }
    }
    val valuePaint = remember(density, primaryColor) {
        Paint().apply {
            isAntiAlias = true
            textSize = density.run { 10.sp.toPx() }
            color = primaryColor.toArgb()
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }
    val labelPaint = remember(density, onSurfaceVariant) {
        Paint().apply {
            isAntiAlias = true
            textSize = density.run { 10.sp.toPx() }
            color = onSurfaceVariant.toArgb()
            textAlign = Paint.Align.CENTER
        }
    }

    val tooltipTextPaint = remember(density, onTooltipColor) {
        Paint().apply {
            isAntiAlias = true
            textSize = density.run { 12.sp.toPx() }
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
                    val yAxisLabelWidth = 36.dp.toPx()
                    val chartAreaWidth = size.width - yAxisLabelWidth
                    val barSpacing = 4.dp.toPx()
                    val totalSpacing = barSpacing * (data.size + 1)
                    val barWidth = (chartAreaWidth - totalSpacing).coerceAtLeast(0f) / data.size

                    val tappedBarIndex = data.indices.firstOrNull { index ->
                        val left = yAxisLabelWidth + barSpacing + index * (barWidth + barSpacing)
                        offset.x >= left && offset.x <= left + barWidth
                    }
                    selectedBar = if (selectedBar == tappedBarIndex) null else tappedBarIndex
                }
            }
    ) {
        val animated = animatedProgress.value
        val yAxisLabelWidth = 36.dp.toPx()
        val xAxisLabelHeight = 36.dp.toPx()
        val valueLabelHeight = 18.dp.toPx()
        val chartAreaWidth = size.width - yAxisLabelWidth
        val chartAreaHeight = size.height - xAxisLabelHeight - valueLabelHeight

        drawGrid(yAxisLabelWidth, chartAreaHeight, valueLabelHeight, maxValue, textPaint, outlineVariant)

        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (data.size + 1)
        val barWidth = (chartAreaWidth - totalSpacing).coerceAtLeast(0f) / data.size
        val barRects = mutableListOf<Rect>()

        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value.toFloat() / maxValue) * chartAreaHeight * animated
            val left = yAxisLabelWidth + barSpacing + index * (barWidth + barSpacing)
            val top = valueLabelHeight + chartAreaHeight - barHeight
            barRects.add(Rect(left = left, top = top, right = left + barWidth, bottom = top + barHeight))

            drawRoundRect(
                color = surfaceVariant.copy(alpha = 0.2f),
                topLeft = Offset(left, valueLabelHeight),
                size = Size(barWidth, chartAreaHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            if (barHeight > 0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(primaryColor, secondaryColor)),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            val valueTextY = top - 4.dp.toPx()
            if (selectedBar != index) { // Não desenha o valor se o tooltip estiver visível
                drawContext.canvas.nativeCanvas.drawText(value.toString(), barRects[index].center.x, valueTextY, valuePaint)
            }

            val labelTextY = size.height - xAxisLabelHeight + 12.dp.toPx()
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(45f, barRects[index].center.x, labelTextY)
            drawContext.canvas.nativeCanvas.drawText(label, barRects[index].center.x, labelTextY, labelPaint)
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
    val tooltipWidth = 60.dp.toPx()
    val tooltipHeight = 24.dp.toPx()
    val cornerRadius = CornerRadius(6.dp.toPx())

    val tooltipRect = Rect(
        left = barRect.center.x - tooltipWidth / 2,
        top = barRect.top - tooltipHeight - 8.dp.toPx(),
        right = barRect.center.x + tooltipWidth / 2,
        bottom = barRect.top - 8.dp.toPx()
    )

    drawRoundRect(
        color = tooltipColor,
        topLeft = tooltipRect.topLeft,
        size = tooltipRect.size,
        cornerRadius = cornerRadius
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
    val gridLines = 4
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))

    (0..gridLines).forEach { i ->
        val y = topPadding + chartAreaHeight * (1f - i.toFloat() / gridLines)
        val value = (maxValue * i.toFloat() / gridLines).roundToInt()
        drawLine(
            color = lineColor.copy(alpha = 0.5f),
            start = Offset(yAxisLabelWidth, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect
        )
        val textY = y + (textPaint.descent() - textPaint.ascent()) / 2 - textPaint.descent()
        drawContext.canvas.nativeCanvas.drawText(
            value.toString(),
            yAxisLabelWidth - 4.dp.toPx(),
            textY,
            textPaint
        )
    }
}