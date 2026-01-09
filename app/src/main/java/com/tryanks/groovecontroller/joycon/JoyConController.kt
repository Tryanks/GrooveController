package com.tryanks.groovecontroller.joycon

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.content.Context
import android.util.Log

class JoyConController(
    private val context: Context,
    private val type: ControllerType,
    private val hidProxy: BluetoothHidDevice,
    private val macAddress: String
) {
    private val TAG = "JoyConController"
    private val state = JoyConState(parseMacAddress(macAddress))
    private val memory = JoyConMemory(context, type)

    fun handleOutputReport(device: BluetoothDevice, reportId: Byte, data: ByteArray) {
        when (reportId) {
            JoyConConstants.REQUEST_RUMBLE_AND_SUBCOMMAND -> {
                handleSubcommand(device, data)
            }
            JoyConConstants.REQUEST_RUMBLE_ONLY -> {
                // Ignore rumble for now
            }
        }
    }

    private val currentButtons = ByteArray(3) // Right, Shared, Left
    private val currentStickLeft = shortArrayOf(0x800, 0x800) // X, Y (Centered)
    private val currentStickRight = shortArrayOf(0x800, 0x800)

    fun sendGrooveReport(device: BluetoothDevice, event: com.tryanks.groovecontroller.ControlEvent, controlType: com.tryanks.groovecontroller.ControlType) {
        // Reset state
        currentButtons[0] = 0
        currentButtons[1] = 0
        currentButtons[2] = 0
        currentStickLeft[0] = 0x800
        currentStickLeft[1] = 0x800
        currentStickRight[0] = 0x800
        currentStickRight[1] = 0x800

        val isLeft = controlType == com.tryanks.groovecontroller.ControlType.Left
        
        // Map Groove Coaster events to Joy-Con
        when (event) {
            com.tryanks.groovecontroller.ControlEvent.Up -> if (isLeft) currentStickLeft[1] = 0xFFF else currentStickRight[1] = 0xFFF
            com.tryanks.groovecontroller.ControlEvent.Down -> if (isLeft) currentStickLeft[1] = 0x000 else currentStickRight[1] = 0x000
            com.tryanks.groovecontroller.ControlEvent.Left -> if (isLeft) currentStickLeft[0] = 0x000 else currentStickRight[0] = 0x000
            com.tryanks.groovecontroller.ControlEvent.Right -> if (isLeft) currentStickLeft[0] = 0xFFF else currentStickRight[0] = 0xFFF
            com.tryanks.groovecontroller.ControlEvent.UpLeft -> {
                if (isLeft) { currentStickLeft[0] = 0x000; currentStickLeft[1] = 0xFFF } 
                else { currentStickRight[0] = 0x000; currentStickRight[1] = 0xFFF }
            }
            com.tryanks.groovecontroller.ControlEvent.UpRight -> {
                if (isLeft) { currentStickLeft[0] = 0xFFF; currentStickLeft[1] = 0xFFF } 
                else { currentStickRight[0] = 0xFFF; currentStickRight[1] = 0xFFF }
            }
            com.tryanks.groovecontroller.ControlEvent.DownLeft -> {
                if (isLeft) { currentStickLeft[0] = 0x000; currentStickLeft[1] = 0x000 } 
                else { currentStickRight[0] = 0x000; currentStickRight[1] = 0x000 }
            }
            com.tryanks.groovecontroller.ControlEvent.DownRight -> {
                if (isLeft) { currentStickLeft[0] = 0xFFF; currentStickLeft[1] = 0x000 } 
                else { currentStickRight[0] = 0xFFF; currentStickRight[1] = 0x000 }
            }
            com.tryanks.groovecontroller.ControlEvent.Tap -> {
                // Map Tap to L/R or ZL/ZR
                if (isLeft) currentButtons[2] = JoyConConstants.FULL_L_R_BIT else currentButtons[0] = JoyConConstants.FULL_L_R_BIT
            }
            else -> {}
        }

        val reportId: Byte
        val reportData: ByteArray

        if (state.inputReportMode == InputReportMode.STANDARD_FULL_MODE) {
            reportId = 0x30
            reportData = ByteArray(48)
            reportData[0] = state.getNextTime()
            reportData[1] = 0x8E.toByte() // Battery
            
            // Buttons
            reportData[2] = currentButtons[0]
            reportData[3] = currentButtons[1]
            reportData[4] = currentButtons[2]
            
            // Sticks (Left)
            reportData[5] = (currentStickLeft[0].toInt() and 0xFF).toByte()
            reportData[6] = ((currentStickLeft[0].toInt() shr 8) or ((currentStickLeft[1].toInt() and 0x0F) shl 4)).toByte()
            reportData[7] = ((currentStickLeft[1].toInt() shr 4) and 0xFF).toByte()

            // Sticks (Right)
            reportData[8] = (currentStickRight[0].toInt() and 0xFF).toByte()
            reportData[9] = ((currentStickRight[0].toInt() shr 8) or ((currentStickRight[1].toInt() and 0x0F) shl 4)).toByte()
            reportData[10] = ((currentStickRight[1].toInt() shr 4) and 0xFF).toByte()
            
            // Vibrator ACK
            reportData[11] = 0x00
            
            // IMU data (dummy)
            // ...
        } else {
            // Simple HID mode 0x3F
            reportId = 0x3F
            reportData = ByteArray(11)
            // Map buttons to simple HID bits
            var btnLow = 0
            if (currentButtons[2] != 0.toByte()) btnLow = btnLow or 0x40 // L
            if (currentButtons[0] != 0.toByte()) btnLow = btnLow or 0x80 // R
            reportData[0] = btnLow.toByte()
            reportData[1] = 0 // Other buttons
            
            // Hat switch (directions)
            var hat: Byte = 0x08 // Centered
            if (isLeft) {
                hat = when(event) {
                    com.tryanks.groovecontroller.ControlEvent.Up -> 0
                    com.tryanks.groovecontroller.ControlEvent.UpRight -> 1
                    com.tryanks.groovecontroller.ControlEvent.Right -> 2
                    com.tryanks.groovecontroller.ControlEvent.DownRight -> 3
                    com.tryanks.groovecontroller.ControlEvent.Down -> 4
                    com.tryanks.groovecontroller.ControlEvent.DownLeft -> 5
                    com.tryanks.groovecontroller.ControlEvent.Left -> 6
                    com.tryanks.groovecontroller.ControlEvent.UpLeft -> 7
                    else -> 8
                }.toByte()
            }
            reportData[2] = hat
            
            // Stick data
            reportData[3] = 0x80.toByte() // LX
            reportData[4] = 0x80.toByte() // LY
            reportData[5] = 0x80.toByte() // RX
            reportData[6] = 0x80.toByte() // RY
        }

        hidProxy.sendReport(device, reportId.toInt(), reportData)
    }

    private fun handleSubcommand(device: BluetoothDevice, data: ByteArray) {
        if (data.size < 10) return
        val subcommandId = data[9]
        val response = ByteArray(48) // Default size for 0x21 report

        // Fill Common Header for 0x21 report
        response[0] = 0x21
        response[1] = state.getNextTime()
        response[2] = 0x8E.toByte() // Battery + Connection (Full + Switch)
        // Stick data (default centered)
        response[3] = 0x00
        response[4] = 0x00
        response[5] = 0x00
        response[6] = 0x00
        response[7] = 0x00
        response[8] = 0x00

        response[12] = (subcommandId.toInt() or 0x80).toByte() // ACK

        when (subcommandId) {
            JoyConConstants.REQUEST_DEVICE_INFO -> {
                Log.d(TAG, "Subcommand: REQUEST_DEVICE_INFO")
                response[13] = 0x03 // Firmware Major
                response[14] = 0x48 // Firmware Minor
                response[15] = type.typeByte
                response[16] = 0x02 // Unknown
                // MAC Address (6 bytes)
                System.arraycopy(state.macBytes, 0, response, 17, 6)
                response[23] = 0x01 // Unknown
                response[24] = 0x01 // Use Colors from SPI
            }
            JoyConConstants.REQUEST_INPUT_REPORT_MODE -> {
                val modeByte = data[10]
                state.inputReportMode = InputReportMode.getInputReportMode(modeByte)
                Log.d(TAG, "Subcommand: REQUEST_INPUT_REPORT_MODE to $modeByte")
            }
            JoyConConstants.REQUEST_SPI_FLASH_READ -> {
                val offset = (data[10].toInt() and 0xFF) or
                        ((data[11].toInt() and 0xFF) shl 8) or
                        ((data[12].toInt() and 0xFF) shl 16) or
                        ((data[13].toInt() and 0xFF) shl 24)
                val length = data[14].toInt() and 0xFF
                Log.d(TAG, "Subcommand: SPI_FLASH_READ offset=0x${Integer.toHexString(offset)}, length=$length")
                System.arraycopy(data, 10, response, 13, 4) // Copy back offset
                response[17] = length.toByte()
                val spiData = memory.read(offset, length)
                System.arraycopy(spiData, 0, response, 18, spiData.size)
            }
            JoyConConstants.REQUEST_SET_PLAYER_LIGHTS -> {
                state.playerLights = data[10]
                Log.d(TAG, "Subcommand: REQUEST_SET_PLAYER_LIGHTS ${state.playerLights}")
            }
            JoyConConstants.REQUEST_AXIS_SENSOR -> {
                state.axisSensorEnabled = data[10] == 0x01.toByte()
                Log.d(TAG, "Subcommand: REQUEST_AXIS_SENSOR ${state.axisSensorEnabled}")
            }
            JoyConConstants.SET_IMU_SENSITIVITY -> {
                Log.d(TAG, "Subcommand: SET_IMU_SENSITIVITY")
            }
            JoyConConstants.REQUEST_VIBRATION -> {
                state.vibrationEnabled = data[10] == 0x01.toByte()
                Log.d(TAG, "Subcommand: REQUEST_VIBRATION ${state.vibrationEnabled}")
            }
            else -> {
                Log.d(TAG, "Unknown Subcommand: 0x${Integer.toHexString(subcommandId.toInt() and 0xFF)}")
            }
        }

        hidProxy.sendReport(device, 0x21, response)
    }

    private fun parseMacAddress(address: String): ByteArray {
        val bytes = ByteArray(6)
        val parts = address.split(":")
        for (i in 0..5) {
            bytes[i] = Integer.parseInt(parts[i], 16).toByte()
        }
        return bytes
    }
}
