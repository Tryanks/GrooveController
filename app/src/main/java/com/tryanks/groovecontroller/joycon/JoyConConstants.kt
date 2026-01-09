package com.tryanks.groovecontroller.joycon

object JoyConConstants {
    const val BUTTON_REPORT: Byte = 0x3F
    const val SUBCOMMAND_REPLY_REPORT: Byte = 0x21
    const val FULL_BUTTON_REPORT: Byte = 0x30
    const val NFC_IR_REPORT: Byte = 0x31
    const val SIMPLE_HID_REPORT: Byte = 0x3F

    // COMMANDS
    const val REQUEST_RUMBLE_AND_SUBCOMMAND: Byte = 0x01
    const val REQUEST_RUMBLE_ONLY: Byte = 0x10
    const val REQUEST_NFC_IR_MCU: Byte = 0x11

    // SUBCOMMANDS
    const val CONTROLLER_STATE: Byte = 0x00
    const val BLUETOOTH_MANUAL_PAIRING: Byte = 0x01
    const val REQUEST_DEVICE_INFO: Byte = 0x02
    const val REQUEST_SET_SHIPMENT: Byte = 0x08
    const val REQUEST_SPI_FLASH_READ: Byte = 0x10
    const val REQUEST_SPI_FLASH_WRITE: Byte = 0x11
    const val REQUEST_INPUT_REPORT_MODE: Byte = 0x03
    const val REQUEST_TRIGGER_BUTTONS: Byte = 0x04
    const val REQUEST_AXIS_SENSOR: Byte = 0x40
    const val SET_IMU_SENSITIVITY: Byte = 0x41
    const val REQUEST_VIBRATION: Byte = 0x48
    const val REQUEST_SET_PLAYER_LIGHTS: Byte = 0x30
    const val REQUEST_SET_NFC_IR_CONFIGURATION: Byte = 0x21
    const val REQUEST_SET_NFC_IR_STATE: Byte = 0x22

    // ACK
    const val ACK: Byte = 0x80.toByte()

    // Button Report Bits
    const val DOWN_BIT: Byte = 0x01
    const val RIGHT_BIT: Byte = 0x02
    const val LEFT_BIT: Byte = 0x04
    const val UP_BIT: Byte = 0x08
    const val SL_BIT: Byte = 0x10
    const val SR_BIT: Byte = 0x20

    const val MINUS_BIT: Byte = 0x01
    const val PLUS_BIT: Byte = 0x02
    const val LEFT_STICK_BIT: Byte = 0x04
    const val RIGHT_STICK_BIT: Byte = 0x08
    const val HOME_BIT: Byte = 0x10
    const val CAPTURE_BIT: Byte = 0x20
    const val L_R_BIT: Byte = 0x40
    const val ZL_ZR_BIT: Byte = 0x80.toByte()

    // Full report bits
    const val FULL_MINUS_BIT: Byte = 0x01
    const val FULL_PLUS_BIT: Byte = 0x02
    const val FULL_RIGHT_STICK_BIT: Byte = 0x04
    const val FULL_LEFT_STICK_BIT: Byte = 0x08
    const val FULL_HOME_BIT: Byte = 0x10
    const val FULL_CAPTURE_BIT: Byte = 0x20

    const val FULL_DOWN_BIT: Byte = 0x01
    const val FULL_RIGHT_BIT: Byte = 0x04
    const val FULL_LEFT_BIT: Byte = 0x08
    const val FULL_UP_BIT: Byte = 0x02
    const val FULL_SL_BIT: Byte = 0x20
    const val FULL_SR_BIT: Byte = 0x10
    const val FULL_L_R_BIT: Byte = 0x40
    const val FULL_ZL_ZR_BIT: Byte = 0x80.toByte()

    const val FULL_Y_BIT: Byte = 0x01
    const val FULL_X_BIT: Byte = 0x02
    const val FULL_B_BIT: Byte = 0x04
    const val FULL_A_BIT: Byte = 0x08
}
