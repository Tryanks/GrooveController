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
        fun toInt(value: ControlEvent) = value.value
    }
}

interface BasicDescriptor {
    val name: String
    val description: String
    val providerName: String
    val descReport: ByteArray
    val subClass: Byte
//    fun SendEvent(e: ControlEvent)
}

class KeyboardDesc(): BasicDescriptor {
    override val name = "Groove Coaster Keyboard"
    override val description = "A Controller emulator for Groove Coaster"
    override val providerName = "Tryanks"
    override val subClass: Byte = BluetoothHidDevice.SUBCLASS1_KEYBOARD
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
}

class JoystickDesc(): BasicDescriptor {
    override val name = "Groove Coaster Joystick"
    override val description = "A Controller emulator for Groove Coaster"
    override val providerName = "Tryanks"
    override val subClass: Byte = BluetoothHidDevice.SUBCLASS2_JOYSTICK
    override val descReport: ByteArray = intArrayOf(
        0x05, 0x01, // Usage Page (Generic Desktop)
        0x09, 0x04, // Usage (Joystick)
        0xA1, 0x01, // Collection (Application)
        0x85, 0x01, //   Report ID (1)
        0x05, 0x01, //   Usage Page (Generic Desktop)
        0x09, 0x30, //   Usage (X)
        0x09, 0x31, //   Usage (Y)
        0x09, 0x32, //   Usage (Z)
        0x09, 0x35, //   Usage (Rz)
        0x15, 0x00, //   Logical Minimum (0)
        0x26, 0xFF, 0x00, //   Logical Maximum (255)
        0x75, 0x08, //   Report Size (8)
        0x95, 0x04, //   Report Count (4)
        0x81, 0x02, //   Input (Data, Variable, Absolute)
        0x05, 0x09, //   Usage Page (Button)
        0x19, 0x01, //   Usage Minimum (1)
        0x29, 0x0E, //   Usage Maximum (14)
        0x15, 0x00, //   Logical Minimum (0)
        0x25, 0x01, //   Logical Maximum (1)
        0x75, 0x01, //   Report Size (1)
        0x95, 0x0E, //   Report Count (14)
        0x81, 0x02, //   Input (Data, Variable, Absolute)
        0x05, 0x01, //   Usage Page (Generic Desktop)
        0x09, 0x39, //   Usage (Hat switch)
        0x15, 0x00, //   Logical Minimum (0)
        0x25, 0x07, //   Logical Maximum (7)
        0x35, 0x00, //
    ).map { it.toByte() }.toByteArray()
}
