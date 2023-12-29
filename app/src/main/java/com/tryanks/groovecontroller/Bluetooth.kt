package com.tryanks.groovecontroller

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.util.Log

class Bluetooth {

}

class HidDeviceCallBack: BluetoothHidDevice.Callback() {
    val device: BluetoothHidDevice? = null
    val desc: BasicDescriptor? = null

    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
        if (registered) {
            Log.d("HID", "onAppStatusChanged: HID Device is registered")
        } else {
            Log.d("HID", "onAppStatusChanged: HID Device is unregistered")
        }
    }

    override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {

    }

    override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, data: ByteArray?) {
        super.onInterruptData(device, reportId, data)
    }
}