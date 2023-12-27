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
    var direction by remember { mutableStateOf(ControlEvent.None) }
    val slideStroke = SlideStroke * SlideStroke
    val debounceValue = DebounceValue * DebounceValue
    var startX = 0f
    var startY = 0f
    var currentX = 0f
    var currentY = 0f
    var activePointerId: Int? = null
    var lastDirection = ControlEvent.None

    fun down(it: MotionEvent, id: Int) {
        direction = ControlEvent.Tap
        val index = it.findPointerIndex(id)
        startX = it.getX(index)
        startY = it.getY(index)
        currentX = startX
        currentY = startY
        lastDirection = ControlEvent.None
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
                    direction = ControlEvent.None
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId == null) return@pointerInteropFilter false
                val index = it.findPointerIndex(activePointerId!!)
                val x = it.getX(index)
                val y = it.getY(index)

                // 消抖
                if (slideDistance(x, y, currentX, currentY) < debounceValue) return@pointerInteropFilter false

                if (lastDirection == ControlEvent.None) {
                    // 第一次滑动
                    if (slideDistance(startX, startY, x, y) > slideStroke) {
                        direction = calculateDirection(startX, startY, x, y)
                    }
                    lastDirection = direction
                    currentX = x
                    currentY = y
                } else {
                    // 后续滑动开始判断用户手指是否有变向的趋势
                    val tempDirection = calculateDirection(currentX, currentY, x, y)
                    if (tempDirection == lastDirection) {
                        // 没有变向
                        currentX = x
                        currentY = y
                    } else {
                        // 存在变向的趋势
                        if (slideDistance(currentX, currentY, x, y) > slideStroke) {
                            // 保持变向的趋势并且大于滑动行程
                            direction = tempDirection
                            lastDirection = direction
                        }
                    }
                }
            }
        }
        action(ControlEvent.fromInt(direction.value))
        true
    }) {
        drawArrow(size, 50f, ControlEvent.fromInt(direction.value))
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