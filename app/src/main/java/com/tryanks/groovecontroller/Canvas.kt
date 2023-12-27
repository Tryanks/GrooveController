package com.tryanks.groovecontroller

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawTap(canvasSize: Size, radiusPercentage: Float, strokeWidth: Float) {
    val circleRadius = minOf(canvasSize.width, canvasSize.height) * radiusPercentage
    val circleCenter = Offset(x = canvasSize.width / 2, y = canvasSize.height / 2)
    drawCircle(
        color = Color.White,
        center = circleCenter,
        radius = circleRadius,
        style = Stroke(width = strokeWidth)
    )
}

fun DrawScope.drawArrow(canvasSize: Size, strokeWidth: Float, rotate: ControlEvent) {
    if (rotate == ControlEvent.None) return
    if (rotate == ControlEvent.Tap) {
        drawTap(canvasSize, 0.3f, 50f)
        return
    }

    val canvasCenter = Offset(x = canvasSize.width / 2, y = canvasSize.height / 2)
    val arrowLength = canvasSize.width * 0.7f
    val arrowHeadLength = canvasSize.width * 0.2f
    val arrowHeadWidth = canvasSize.height * 0.4f

    var arrowStart = Offset(x = canvasSize.width * 0.1f, y = canvasSize.height / 2)
    var arrowEnd = Offset(x = arrowStart.x + arrowLength, y = arrowStart.y)
    var arrowHeadLeft = Offset(x = arrowEnd.x - arrowHeadLength, y = arrowEnd.y - arrowHeadWidth / 2)
    var arrowHeadRight = Offset(x = arrowEnd.x - arrowHeadLength, y = arrowEnd.y + arrowHeadWidth / 2)

    arrowStart -= canvasCenter
    arrowEnd -= canvasCenter
    arrowHeadLeft -= canvasCenter
    arrowHeadRight -= canvasCenter

    val rotation = when (rotate) {
        ControlEvent.UpLeft -> -45f
        ControlEvent.UpRight -> 45f
        ControlEvent.Left -> -90f
        ControlEvent.Right -> 90f
        ControlEvent.DownLeft -> -135f
        ControlEvent.Down -> 180f
        ControlEvent.DownRight -> 135f
        else -> 0f
    }

    arrowStart = arrowStart.rotate(rotation)
    arrowEnd = arrowEnd.rotate(rotation)
    arrowHeadLeft = arrowHeadLeft.rotate(rotation)
    arrowHeadRight = arrowHeadRight.rotate(rotation)

    arrowStart += canvasCenter
    arrowEnd += canvasCenter
    arrowHeadLeft += canvasCenter
    arrowHeadRight += canvasCenter

    drawLine(color = Color.White, start = arrowStart, end = arrowEnd, strokeWidth = strokeWidth)

    val arrowHeadPath = Path().apply {
        moveTo(arrowEnd.x, arrowEnd.y)
        lineTo(arrowHeadLeft.x, arrowHeadLeft.y)
        lineTo(arrowEnd.x, arrowEnd.y)
        lineTo(arrowHeadRight.x, arrowHeadRight.y)
    }
    drawPath(color = Color.White, path = arrowHeadPath, style = Stroke(width = strokeWidth))
}

fun Offset.rotate(degrees: Float): Offset {
    val rad = Math.toRadians(degrees.toDouble())
    val sin = sin(rad).toFloat()
    val cos = cos(rad).toFloat()
    return Offset(x = x * cos - y * sin, y = x * sin + y * cos)
}