package com.tryanks.groovecontroller

val KeyboardName = "GrooveController"
val Description = "A Controller emulator for Groove Coaster"
val ProviderName = "Tryanks"
val hidDesc: ByteArray = intArrayOf(
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