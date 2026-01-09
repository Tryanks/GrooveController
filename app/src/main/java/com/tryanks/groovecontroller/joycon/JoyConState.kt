package com.tryanks.groovecontroller.joycon

import java.util.concurrent.atomic.AtomicInteger

class JoyConState(var macBytes: ByteArray) {
    private val timeByte = AtomicInteger(0)
    var inputReportMode: InputReportMode = InputReportMode.SIMPLE_HID
    var playerLights: Byte = 0
    var axisSensorEnabled: Boolean = false
    var vibrationEnabled: Boolean = false

    fun getNextTime(): Byte {
        return (timeByte.incrementAndGet() and 0xFF).toByte()
    }
}
