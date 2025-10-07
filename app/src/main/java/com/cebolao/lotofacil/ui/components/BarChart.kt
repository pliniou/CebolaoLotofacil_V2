package com.cebolao.lotofacil.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
    LaunchedEffect(data) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(700))
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

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

    Canvas(modifier = modifier.height(chartHeight)) {
        val yAxisLabelWidth = 36.dp.toPx()
        val xAxisLabelHeight = 36.dp.toPx()
        val valueLabelHeight = 18.dp.toPx()
        val chartAreaWidth = size.width - yAxisLabelWidth
        val chartAreaHeight = size.height - xAxisLabelHeight - valueLabelHeight

        drawGrid(
            yAxisLabelWidth,
            chartAreaHeight,
            valueLabelHeight,
            maxValue,
            textPaint,
            outlineVariant
        )

        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (data.size + 1)
        val barWidth = (chartAreaWidth - totalSpacing).coerceAtLeast(0f) / data.size

        data.forEachIndexed { index, (label, value) ->
            val barHeight = (value.toFloat() / maxValue) * chartAreaHeight * animatedProgress.value
            val left = yAxisLabelWidth + barSpacing + index * (barWidth + barSpacing)
            val barCenterX = left + barWidth / 2

            drawRoundRect(
                color = surfaceVariant.copy(alpha = 0.2f),
                topLeft = Offset(left, valueLabelHeight),
                size = Size(barWidth, chartAreaHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            if (barHeight > 0) {
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(primaryColor, secondaryColor)),
                    topLeft = Offset(left, valueLabelHeight + chartAreaHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            val valueTextY = valueLabelHeight + chartAreaHeight - barHeight - 4.dp.toPx()
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                barCenterX,
                valueTextY,
                valuePaint
            )

            val labelTextY = size.height - xAxisLabelHeight + 12.dp.toPx()
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.rotate(45f, barCenterX, labelTextY)
            drawContext.canvas.nativeCanvas.drawText(
                label,
                barCenterX,
                labelTextY,
                labelPaint
            )
            drawContext.canvas.nativeCanvas.restore()
        }
    }
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