package com.tryanks.groovecontroller.joycon

enum class InputReportMode(val arg: Byte) {
    ACTIVE_PULLING_NFC_IR_DATA(0x00),
    ACTIVE_PULLING_NFC_IR_MCU(0x01),
    ACTIVE_PULLING_NFC_IR_SPECIFIC(0x02),
    ACTIVE_PULLING_IR_DATA(0x03),
    MCU_UPDATE_STATE(0x23),
    STANDARD_FULL_MODE(0x30),
    NFC_IR_MODE(0x31),
    UNKNOWN_1(0x33),
    UNKNOWN_2(0x35),
    SIMPLE_HID(0x3F),
    UNKNOWN(0xFF.toByte());

    companion object {
        fun getInputReportMode(b: Byte): InputReportMode {
            return values().firstOrNull { it.arg == b } ?: UNKNOWN
        }
    }
}
