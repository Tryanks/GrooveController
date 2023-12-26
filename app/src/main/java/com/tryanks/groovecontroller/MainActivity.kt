package com.tryanks.groovecontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var btAdapter: BluetoothAdapter? = null
    private var hidDevice: BluetoothHidDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
        ), 0)

        btAdapter = bluetoothAdapter()
        callBluetooth()
        if (hidDevice == null) return

    }

    private fun callBluetooth() {
        if (btAdapter == null) return
        val ctx = this
        btAdapter!!.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                checkPermission()
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                }
                registerBluetoothHid(KeyboardReport())
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), 0)
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = null
                }
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    private var hostDevice: BluetoothDevice? = null

    @RequiresApi(Build.VERSION_CODES.S)
    private fun registerBluetoothHid(report: BasicReport) {
        val qosSettings = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800, 9, 0, 11250, BluetoothHidDeviceAppQosSettings.MAX
        )
        val keyboardSdpSettings = BluetoothHidDeviceAppSdpSettings(
            report.name, report.description, report.providerName, report.subClass, report.descReport
        )
        // TODO: BluetoothHidDevice.SUBCLASS2_JOYSTICK

        checkPermission()
        hidDevice!!.registerApp(keyboardSdpSettings, null, qosSettings, Executors.newCachedThreadPool(), object :
            BluetoothHidDevice.Callback() {
                override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                    checkPermission()
                    if (registered) {
                        if (pluggedDevice != null && hidDevice!!.getConnectionState(pluggedDevice) != BluetoothProfile.STATE_CONNECTED) {
                            val result = hidDevice!!.connect(pluggedDevice)
                        }
                    }
                }

                override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        hostDevice = device
                    }
                    if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        hostDevice = null
                    }
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkPermission() {

    }
}

fun Context.bluetoothAdapter(): BluetoothAdapter? =
    (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

class HidDeviceCallBack: BluetoothHidDevice.Callback() {
    val btHidDevice: BluetoothHidDevice? = null
    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
        if (registered) {
            Log.d("HidDeviceCallBack", "registered$pluggedDevice")
        } else {
            Log.d("HidDeviceCallBack", "unregistered$pluggedDevice")
        }
    }

    override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
//        btHidDevice.sendReport(device, type, id, hidReport)
    }

    override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
        super.onConnectionStateChanged(device, state)
    }
}