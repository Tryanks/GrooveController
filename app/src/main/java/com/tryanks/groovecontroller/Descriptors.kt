package com.tryanks.groovecontroller

import android.bluetooth.BluetoothHidDevice

enum class ControlEvent(val value: Int) {
    None(0),
    Tap(1),
    Up(2),
    UpRight(3),
    Right(4),
    DownRight(5),
    Down(6),
    DownLeft(7),
    Left(8),
    UpLeft(9);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}

enum class ControlType(val value: Int) {
    Left(0),
    Right(1);
}

interface BasicDescriptor {
    val name: String
    val description: String
    val providerName: String
    val descReport: ByteArray
    val subClass: Byte
    val reportId: Byte
    fun getReport(e: ControlEvent, t: ControlType): ByteArray
}

class KeyboardDesc: BasicDescriptor {
    override val name = "Groove Coaster Keyboard"
    override val description = "Groove Coaster Controller Emulator"
    override val providerName = "Tryanks"
    override val subClass: Byte = BluetoothHidDevice.SUBCLASS1_KEYBOARD
    override val reportId: Byte = 0
    override val descReport: ByteArray = intArrayOf(
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x06, // Usage (Keyboard)
        0xA1, 0x01, // Collection (Application)
        0x05, 0x07, //   Usage Page (Key Codes)
        0x19, 0xE0, //   Usage Minimum (224)
        0x29, 0xE7, //   Usage Maximum (231)
        0x15, 0x00, //   Logical Minimum (0)
        0x25, 0x01, //   Logical Maximum (1)
        0x75, 0x01, //   Report Size (1)
        0x95, 0x08, //   Report Count (8)
        0x81, 0x02, //   Input (Data, Variable, Absolute): Modifier byte
        0x75, 0x08, //   Report Size (8)
        0x95, 0x01, //   Report Count (1)
        0x81, 0x01, //   Input (Constant): Reserved byte
        0x05, 0x08, //   Usage Page (LEDs)
        0x19, 0x01, //   Usage Minimum (1)
        0x29, 0x05, //   Usage Maximum (5)
        0x75, 0x01, //   Report Size (1)
        0x95, 0x05, //   Report Count (5)
        0x91, 0x02, //   Output (Data, Variable, Absolute): LED report
        0x75, 0x03, //   Report Size (3)
        0x95, 0x01, //   Report Count (1)
        0x91, 0x01, //   Output (Constant): LED report padding
        0x05, 0x07, //   Usage Page (Key Codes)
        0x19, 0x00, //   Usage Minimum (0)
        0x29, 102 - 1, //   Usage Maximum (101)
        0x15, 0x00, //   Logical Minimum (0)
        0x25, 102 - 1, //   Logical Maximum(101)
        0x75, 0x08, //   Report Size (8)
        0x95, 6, //   Report Count (6)
        0x81, 0x00, //   Input (Data, Array): Keys
        0xC0, //       End Collection
    ).map { it.toByte() }.toByteArray()

    private val reportHid = ByteArray(8)
    override fun getReport(e: ControlEvent, t: ControlType): ByteArray = synchronized(reportHid) {
        if (t == ControlType.Left) {
            when (e) {
                ControlEvent.None -> {
                    reportHid[2] = 0x00; reportHid[3] = 0x00
                }
                ControlEvent.Up -> {
                    reportHid[2] = 0x1A; reportHid[3] = 0x00 // W
                }
                ControlEvent.Down -> {
                    reportHid[2] = 0x16; reportHid[3] = 0x00 // S
                }
                ControlEvent.Left -> {
                    reportHid[2] = 0x04; reportHid[3] = 0x00 // A
                }
                ControlEvent.Right -> {
                    reportHid[2] = 0x07; reportHid[3] = 0x00 // D
                }
                ControlEvent.UpRight -> {
                    reportHid[2] = 0x1A; reportHid[3] = 0x07 // W + D
                }
                ControlEvent.DownRight -> {
                    reportHid[2] = 0x16; reportHid[3] = 0x07 // S + D
                }
                ControlEvent.UpLeft -> {
                    reportHid[2] = 0x1A; reportHid[3] = 0x04 // W + A
                }
                ControlEvent.DownLeft -> {
                    reportHid[2] = 0x16; reportHid[3] = 0x04 // S + A
                }
                ControlEvent.Tap -> {
                    reportHid[2] = 0x2C; reportHid[3] = 0x00 // Space
                }
            }
        } else {
            when (e) {
                ControlEvent.None -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x00; reportHid[5] = 0x00
                }
                ControlEvent.Up -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x52; reportHid[5] = 0x00 // Up Arrow
                }
                ControlEvent.Down -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x51; reportHid[5] = 0x00 // Down Arrow
                }
                ControlEvent.Left -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x50; reportHid[5] = 0x00 // Left Arrow
                }
                ControlEvent.Right -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x4F; reportHid[5] = 0x00 // Right Arrow
                }
                ControlEvent.UpRight -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x52; reportHid[5] = 0x4F // Up + Right
                }
                ControlEvent.DownRight -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x51; reportHid[5] = 0x4F // Down + Right
                }
                ControlEvent.UpLeft -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x52; reportHid[5] = 0x50 // Up + Left
                }
                ControlEvent.DownLeft -> {
                    reportHid[0] = 0x00; reportHid[4] = 0x51; reportHid[5] = 0x50 // Down + Left
                }
                ControlEvent.Tap -> {
                    reportHid[0] = 0x10; reportHid[4] = 0x00; reportHid[5] = 0x00 // Right Ctrl
                }
            }
        }
        return reportHid.copyOf()
    }
}

class GamepadDesc : BasicDescriptor {
    override val name = "Groove Coaster Gamepad"
    override val description = "Groove Coaster Controller Emulator (Gamepad)"
    override val providerName = "Tryanks"
    override val subClass: Byte = 0x04 // Gamepad/Joystick
    override val reportId: Byte = 1
    override val descReport: ByteArray = intArrayOf(
        0x05, 0x01, 0x09, 0x05, 0xA1, 0x01, 0x85, 0x01, 0x09, 0x01, 0xA1, 0x00, 0x09, 0x30, 0x09, 0x31,
        0x15, 0x00, 0x27, 0xFF, 0xFF, 0x00, 0x00, 0x95, 0x02, 0x75, 0x10, 0x81, 0x02, 0xC0, 0x09, 0x01,
        0xA1, 0x00, 0x09, 0x32, 0x09, 0x35, 0x15, 0x00, 0x27, 0xFF, 0xFF, 0x00, 0x00, 0x95, 0x02, 0x75,
        0x10, 0x81, 0x02, 0xC0, 0x05, 0x02, 0x09, 0xC5, 0x15, 0x00, 0x26, 0xFF, 0x03, 0x95, 0x01, 0x75,
        0x0A, 0x81, 0x02, 0x15, 0x00, 0x25, 0x00, 0x75, 0x06, 0x95, 0x01, 0x81, 0x03, 0x05, 0x02, 0x09,
        0xC4, 0x15, 0x00, 0x26, 0xFF, 0x03, 0x95, 0x01, 0x75, 0x0A, 0x81, 0x02, 0x15, 0x00, 0x25, 0x00,
        0x75, 0x06, 0x95, 0x01, 0x81, 0x03, 0x05, 0x01, 0x09, 0x39, 0x15, 0x01, 0x25, 0x08, 0x35, 0x00,
        0x46, 0x3B, 0x01, 0x66, 0x14, 0x00, 0x75, 0x04, 0x95, 0x01, 0x81, 0x42, 0x75, 0x04, 0x95, 0x01,
        0x15, 0x00, 0x25, 0x00, 0x35, 0x00, 0x45, 0x00, 0x65, 0x00, 0x81, 0x03, 0x05, 0x09, 0x19, 0x01,
        0x29, 0x0F, 0x15, 0x00, 0x25, 0x01, 0x75, 0x01, 0x95, 0x0F, 0x81, 0x02, 0x15, 0x00, 0x25, 0x00,
        0x75, 0x01, 0x95, 0x01, 0x81, 0x03, 0x05, 0x0C, 0x0A, 0x24, 0x02, 0x15, 0x00, 0x25, 0x01, 0x95,
        0x01, 0x75, 0x01, 0x81, 0x02, 0x15, 0x00, 0x25, 0x00, 0x75, 0x07, 0x95, 0x01, 0x81, 0x03, 0x05,
        0x0C, 0x09, 0x01, 0x85, 0x02, 0xA1, 0x01, 0x05, 0x0C, 0x0A, 0x23, 0x02, 0x15, 0x00, 0x25, 0x01,
        0x95, 0x01, 0x75, 0x01, 0x81, 0x02, 0x15, 0x00, 0x25, 0x00, 0x75, 0x07, 0x95, 0x01, 0x81, 0x03,
        0xC0, 0x05, 0x0F, 0x09, 0x21, 0x85, 0x03, 0xA1, 0x02, 0x09, 0x97, 0x15, 0x00, 0x25, 0x01, 0x75,
        0x04, 0x95, 0x01, 0x91, 0x02, 0x15, 0x00, 0x25, 0x00, 0x75, 0x04, 0x95, 0x01, 0x91, 0x03, 0x09,
        0x70, 0x15, 0x00, 0x25, 0x64, 0x75, 0x08, 0x95, 0x04, 0x91, 0x02, 0x09, 0x50, 0x66, 0x01, 0x10,
        0x55, 0x0E, 0x15, 0x00, 0x26, 0xFF, 0x00, 0x75, 0x08, 0x95, 0x01, 0x91, 0x02, 0x09, 0xA7, 0x15,
        0x00, 0x26, 0xFF, 0x00, 0x75, 0x08, 0x95, 0x01, 0x91, 0x02, 0x65, 0x00, 0x55, 0x00, 0x09, 0x7C,
        0x15, 0x00, 0x26, 0xFF, 0x00, 0x75, 0x08, 0x95, 0x01, 0x91, 0x02, 0xC0, 0x05, 0x06, 0x09, 0x20,
        0x85, 0x04, 0x15, 0x00, 0x26, 0xFF, 0x00, 0x75, 0x08, 0x95, 0x01, 0x81, 0x02, 0xC0
    ).map { it.toByte() }.toByteArray()

    private val reportHid = ByteArray(16).apply {
        // Initial values for X, Y, Z, Rz (center)
        val center = 32767
        this[0] = (center and 0xFF).toByte()
        this[1] = ((center shr 8) and 0xFF).toByte()
        this[2] = (center and 0xFF).toByte()
        this[3] = ((center shr 8) and 0xFF).toByte()
        this[4] = (center and 0xFF).toByte()
        this[5] = ((center shr 8) and 0xFF).toByte()
        this[6] = (center and 0xFF).toByte()
        this[7] = ((center shr 8) and 0xFF).toByte()
    }

    override fun getReport(e: ControlEvent, t: ControlType): ByteArray = synchronized(reportHid) {
        val center = 32767
        val min = 0
        val max = 65535

        if (t == ControlType.Left) {
            var x = center
            var y = center
            var buttonA = false
            when (e) {
                ControlEvent.None -> {}
                ControlEvent.Tap -> {
                    buttonA = true
                }
                ControlEvent.Up -> {
                    y = min
                }
                ControlEvent.Down -> {
                    y = max
                }
                ControlEvent.Left -> {
                    x = min
                }
                ControlEvent.Right -> {
                    x = max
                }
                ControlEvent.UpRight -> {
                    x = max; y = min
                }
                ControlEvent.DownRight -> {
                    x = max; y = max
                }
                ControlEvent.UpLeft -> {
                    x = min; y = min
                }
                ControlEvent.DownLeft -> {
                    x = min; y = max
                }
            }
            reportHid[0] = (x and 0xFF).toByte()
            reportHid[1] = ((x shr 8) and 0xFF).toByte()
            reportHid[2] = (y and 0xFF).toByte()
            reportHid[3] = ((y shr 8) and 0xFF).toByte()

            if (buttonA) {
                reportHid[13] = (reportHid[13].toInt() or 0x01).toByte()
            } else {
                reportHid[13] = (reportHid[13].toInt() and 0xFE).toByte()
            }
        } else {
            var z = center
            var rz = center
            var buttonB = false
            when (e) {
                ControlEvent.None -> {}
                ControlEvent.Tap -> {
                    buttonB = true
                }
                ControlEvent.Up -> {
                    rz = min
                }
                ControlEvent.Down -> {
                    rz = max
                }
                ControlEvent.Left -> {
                    z = min
                }
                ControlEvent.Right -> {
                    z = max
                }
                ControlEvent.UpRight -> {
                    z = max; rz = min
                }
                ControlEvent.DownRight -> {
                    z = max; rz = max
                }
                ControlEvent.UpLeft -> {
                    z = min; rz = min
                }
                ControlEvent.DownLeft -> {
                    z = min; rz = max
                }
            }
            reportHid[4] = (z and 0xFF).toByte()
            reportHid[5] = ((z shr 8) and 0xFF).toByte()
            reportHid[6] = (rz and 0xFF).toByte()
            reportHid[7] = ((rz shr 8) and 0xFF).toByte()

            if (buttonB) {
                reportHid[13] = (reportHid[13].toInt() or 0x02).toByte()
            } else {
                reportHid[13] = (reportHid[13].toInt() and 0xFD).toByte()
            }
        }
        return reportHid.copyOf()
    }
}
