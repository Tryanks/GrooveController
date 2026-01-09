package com.tryanks.groovecontroller.joycon

import android.bluetooth.BluetoothHidDevice
import com.tryanks.groovecontroller.BasicDescriptor
import com.tryanks.groovecontroller.ControlEvent
import com.tryanks.groovecontroller.ControlType

class JoyConDesc(val type: ControllerType) : BasicDescriptor {
    override val name: String = type.btName
    override val description: String = ControllerType.HID_DESCRIPTION
    override val providerName: String = ControllerType.HID_PROVIDER
    override val subClass: Byte = ControllerType.SUBCLASS
    override val reportId: Byte = 0 // Not strictly used by sendReport if we specify it

    override val descReport: ByteArray = hexStringToByteArray(ControllerType.DESCRIPTOR)

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    override fun getReport(e: ControlEvent, t: ControlType): ByteArray {
        // This is a simplified report for Groove Coaster. 
        // We will return a 0x3F (Simple HID) report here by default, 
        // or a 0x30 report if we are in full mode.
        // However, JoyConController will handle the actual state.
        
        // For now, let's just return a dummy 0x3F report as a placeholder.
        // The actual logic should probably be in JoyConController.
        return ByteArray(12)
    }
}
