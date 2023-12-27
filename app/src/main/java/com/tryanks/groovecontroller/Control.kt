package com.tryanks.groovecontroller

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import kotlin.math.atan2

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Control(modifier: Modifier = Modifier, action: (ControlEvent) -> Unit = {}) {
    var rotate by remember { mutableStateOf(ControlEvent.None) }
    val slideStroke = SlideStroke * SlideStroke
    val debounceValue = DebounceValue * DebounceValue
    var startX = 0f
    var startY = 0f
    var currentX = 0f
    var currentY = 0f
    var activePointerId: Int? = null

    fun down(it: MotionEvent, id: Int) {
        rotate = ControlEvent.Tap
        val index = it.findPointerIndex(id)
        startX = it.getX(index)
        startY = it.getY(index)
        currentX = startX
        currentY = startY
    }

    Canvas(modifier.pointerInteropFilter {
        when (it.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val actionIndex = it.actionIndex
                val id = it.getPointerId(actionIndex)
                if (activePointerId == null) {
                    activePointerId = id
                    down(it, id)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val actionIndex = it.actionIndex
                val id = it.getPointerId(actionIndex)
                if (id == activePointerId) {
                    activePointerId = null
                    rotate = ControlEvent.None
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == null) return@pointerInteropFilter false

                val index = it.findPointerIndex(activePointerId!!)
                val x = it.getX(index)
                val y = it.getY(index)

                if (slideDistance(x, y, currentX, currentY) < debounceValue) return@pointerInteropFilter false
                else {
                    val tempRotate = calculateDirection(currentX, currentY, x, y)
                    // TODO: 如果移动方向不同，则等待进一步动作，来判断是否存在变向，并等待触发

                }
                if (slideDistance(startX, startY, currentX, currentY) > slideStroke) {
                    rotate = calculateDirection(startX, startY, currentX, currentY)
                }
            }
        }
        action(ControlEvent.fromInt(rotate.value))
        true
    }) {
        drawArrow(size, 50f, ControlEvent.fromInt(rotate.value))
    }
}

fun slideDistance(startX: Float, startY: Float, currentX: Float, currentY: Float): Float {
    val x = currentX - startX
    val y = currentY - startY
    return x * x + y * y
}

fun calculateDirection(startX: Float, startY: Float, currentX: Float, currentY: Float): ControlEvent {
    val angle = atan2((currentY - startY).toDouble(), (currentX - startX).toDouble())
    val degree = Math.toDegrees(angle)
    val direction = ((degree + 22.5 + 3600) / 45).toInt() % 8 + 2
    return ControlEvent.fromInt(direction)
}