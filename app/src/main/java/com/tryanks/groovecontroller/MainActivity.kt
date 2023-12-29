package com.tryanks.groovecontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.tryanks.groovecontroller.ui.theme.GrooveControllerTheme
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    var hid by remember { mutableStateOf<BasicDescriptor?>(null) }
    var hidDevice: BluetoothHidDevice? = null
    var hostDevice: BluetoothDevice? = null
    var ctx: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hid = KeyboardDesc()

        setContent {
            GrooveCoaster()
        }

        // 在这里暂时实现蓝牙 HID 并后续移动
        ctx = this
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(this, object: BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy!! as BluetoothHidDevice
                    val qosSettings = BluetoothHidDeviceAppQosSettings(BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
                        800,9,0,11250,BluetoothHidDeviceAppQosSettings.MAX)
                    val sdpSettings = BluetoothHidDeviceAppSdpSettings(
                        hid!!.name, hid!!.description, hid!!.providerName, hid!!.subClass, hid!!.descReport
                    )

                    if (ActivityCompat.checkSelfPermission(ctx!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }

                    hidDevice!!.registerApp(sdpSettings, null, qosSettings, Executors.newCachedThreadPool(), object:
                        BluetoothHidDevice.Callback() {
                        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                            if (registered) {
                                if (ActivityCompat.checkSelfPermission(ctx!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    return
                                }
                                if (pluggedDevice != null && hidDevice!!.getConnectionState(pluggedDevice) != BluetoothProfile.STATE_CONNECTED) {
                                    hidDevice!!.connect(pluggedDevice)
                                }
                            } else {
                                Log.d("HID", "onAppStatusChanged: HID Device is unregistered")
                            }
                        }

                        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                            if (state == BluetoothProfile.STATE_CONNECTED) {
                                hostDevice = device
                            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                                hostDevice = null
                            }
                        }
                    })

                    startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), 1)
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                TODO("Not yet implemented")
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    @Composable
    fun GrooveCoaster() {
        GrooveControllerTheme(true) {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Control(Modifier.fillMaxWidth().weight(1f)) {
                        val report = hid!!.getReport(it, if (Orientation == 0) ControlType.Left else ControlType.Right)
                        Log.d("LEFT", "controlView: ${report.map { it.toInt() }.joinToString()}")
                        if (ActivityCompat.checkSelfPermission(ctx!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return@Control
                        }
                        hidDevice?.sendReport(hostDevice, report.size, report)
                    }
                    Divider(color = Color.White, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
                    Control(Modifier.fillMaxWidth().weight(1f)) {
                        val report = hid!!.getReport(it, if (Orientation != 0) ControlType.Left else ControlType.Right)
                        Log.d("RIGHT", "controlView: ${report.map { it.toInt() }.joinToString()}")
                        if (ActivityCompat.checkSelfPermission(ctx!!, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return@Control
                        }
                        hidDevice?.sendReport(hostDevice, report.size, report)
                    }
                }
            }
        }
    }
}