package com.tryanks.groovecontroller.joycon

import com.tryanks.groovecontroller.R

enum class ControllerType(
    val btName: String,
    val typeByte: Byte,
    val memoryResource: Int
) {
    LEFT_JOYCON("Joy-Con (L)", 0x01, R.raw.left_joycon_eeprom),
    RIGHT_JOYCON("Joy-Con (R)", 0x02, R.raw.right_joycon_eeprom),
    PRO_CONTROLLER("Pro Controller", 0x03, R.raw.pro_controller_eeprom);

    companion object {
        const val SUBCLASS: Byte = 0x08
        const val HID_NAME = "Wireless Gamepad"
        const val HID_DESCRIPTION = "Gamepad"
        const val HID_PROVIDER = "Nintendo"
        const val DESCRIPTOR =
            "05010905a1010601ff852109217508953081028530093075089530810285310931750896690181028532" +
                    "0932750896690181028533093375089669018102853f0509190129101500250175019510810205010939" +
                    "1500250775049501814205097504950181010501093009310933093416000027ffff0000751095048102" +
                    "0601ff850109017508953091028510091075089530910285110911750895309102851209127508953091" +
                    "02c0"
    }
}
