package com.tryanks.groovecontroller

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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val btAdapter: BluetoothAdapter? = bluetoothAdapter()
    private var hidDevice: BluetoothHidDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callBluetooth()
        if (hidDevice == null) return

    }

    private fun callBluetooth() {
        if (btAdapter == null) return
        val ctx = this
        btAdapter.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            @RequiresApi(Build.VERSION_CODES.S)
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                val check = checkPermission()
                if (check.isNotEmpty()) {
                    ActivityCompat.requestPermissions(ctx, check.split(" ").toTypedArray(), 0)
                }
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                }
                registerBluetoothHid()
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
    private fun registerBluetoothHid() {
        val qosSettings = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800, 9, 0, 11250, BluetoothHidDeviceAppQosSettings.MAX
        )
        val keyboardSdpSettings = BluetoothHidDeviceAppSdpSettings(
            KeyboardName, Description, ProviderName, BluetoothHidDevice.SUBCLASS1_KEYBOARD, hidDesc
        )
        // TODO: BluetoothHidDevice.SUBCLASS2_JOYSTICK

        val check = checkPermission()
        if (check.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, check.split(" ").toTypedArray(), 0)
        }
        val ctx = this
        hidDevice!!.registerApp(keyboardSdpSettings, null, qosSettings, Executors.newCachedThreadPool(), object :
            BluetoothHidDevice.Callback() {
                override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                    val check = checkPermission()
                    if (check.isNotEmpty()) {
                        ActivityCompat.requestPermissions(ctx, check.split(" ").toTypedArray(), 0)
                    }
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
    private fun checkPermission(): String {
        var permissionMessage = ""
        arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
        ).forEach {
            if (ActivityCompat.checkSelfPermission(this, it)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionMessage += "$it "
            }

        }
        return permissionMessage
    }
}

fun Context.bluetoothAdapter(): BluetoothAdapter? =
    (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter