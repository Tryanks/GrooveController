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
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.tryanks.groovecontroller.joycon.ControllerType
import com.tryanks.groovecontroller.joycon.JoyConController
import com.tryanks.groovecontroller.joycon.JoyConDesc
import com.tryanks.groovecontroller.ui.theme.GrooveControllerTheme
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

enum class AppScreen {
    Setup, Controller
}

class MainActivity : ComponentActivity() {
    private var hid by mutableStateOf<BasicDescriptor?>(KeyboardDesc())
    private var hidDevice by mutableStateOf<BluetoothHidDevice?>(null)
    private var hostDevice by mutableStateOf<BluetoothDevice?>(null)
    private var joyConController: JoyConController? = null
    private var isRegistered by mutableStateOf(false)
    private var currentScreen by mutableStateOf(AppScreen.Setup)
    private var bluetoothAddress by mutableStateOf("00:11:22:33:44:55")
    private var showMacDialog by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            initBluetooth()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("groove_prefs", Context.MODE_PRIVATE)
        val savedMac = prefs.getString("bluetooth_mac", null)
        if (savedMac != null) {
            bluetoothAddress = savedMac
        } else {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
            if (adapter != null && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                adapter.address?.let {
                    if (it != "02:00:00:00:00:00") {
                        bluetoothAddress = it
                    }
                }
            }
        }

        setContent {
            GrooveControllerTheme(true) {
                Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                    if (currentScreen == AppScreen.Setup) {
                        SetupScreen()
                    } else {
                        GrooveCoaster()
                    }
                }
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val neededPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        if (neededPermissions.all {
                ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            initBluetooth()
        } else {
            requestPermissionLauncher.launch(neededPermissions)
        }
    }

    private fun initBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (adapter == null) {
            Log.e("HID", "Bluetooth adapter not available")
            return
        }

        adapter.getProfileProxy(this, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = proxy as BluetoothHidDevice
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    hidDevice = null
                    isRegistered = false
                }
            }
        }, BluetoothProfile.HID_DEVICE)
    }

    private fun registerApp() {
        val device = hidDevice ?: return
        val currentHid = hid ?: return

        val qosSettings = BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_GUARANTEED,
            800, 9, 800, 5000, BluetoothHidDeviceAppQosSettings.MAX
        )
        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
            currentHid.name, currentHid.description, currentHid.providerName, currentHid.subClass, currentHid.descReport
        )

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (currentHid is JoyConDesc) {
            val adapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            adapter.name = currentHid.type.btName
            joyConController = JoyConController(this, currentHid.type, device, bluetoothAddress)
        }

        device.registerApp(sdpSettings, null, qosSettings, Executors.newCachedThreadPool(), object :
            BluetoothHidDevice.Callback() {
            override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
                Log.d("HID", "onAppStatusChanged: registered=$registered")
                isRegistered = registered
                if (registered) {
                    if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    if (pluggedDevice != null && device.getConnectionState(pluggedDevice) != BluetoothProfile.STATE_CONNECTED) {
                        device.connect(pluggedDevice)
                    }
                }
            }

            override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
                Log.d("HID", "onConnectionStateChanged: device=$device state=$state")
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    hostDevice = device
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    hostDevice = null
                }
            }

            override fun onInterruptData(device: BluetoothDevice, reportId: Byte, data: ByteArray) {
                joyConController?.handleOutputReport(device, reportId, data)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            hidDevice?.unregisterApp()
        }
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
    }

    @Composable
    fun SetupScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.setup_title), color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(32.dp))

            StatusRow(stringResource(R.string.hid_service), if (hidDevice != null) stringResource(R.string.ready) else stringResource(R.string.not_ready), if (hidDevice != null) Color.Green else Color.Red)
            
            if (hid is JoyConDesc) {
                StatusRow(
                    stringResource(R.string.bluetooth_mac),
                    bluetoothAddress,
                    if (bluetoothAddress == "02:00:00:00:00:00") Color.Yellow else Color.White,
                    modifier = Modifier.clickable { if (!isRegistered) showMacDialog = true }
                )
                if (bluetoothAddress == "02:00:00:00:00:00") {
                    Text(
                        stringResource(R.string.mac_hidden_hint),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                        lineHeight = 16.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.simulation_mode), color = Color.LightGray)
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        TextButton(
                            onClick = { hid = KeyboardDesc() },
                            enabled = !isRegistered,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (hid is KeyboardDesc) Color.Cyan else Color.Gray
                            )
                        ) {
                            Text(stringResource(R.string.keyboard))
                        }
                        TextButton(
                            onClick = { hid = GamepadDesc() },
                            enabled = !isRegistered,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (hid is GamepadDesc) Color.Cyan else Color.Gray
                            )
                        ) {
                            Text(stringResource(R.string.gamepad))
                        }
                    }
                    Row {
                        TextButton(
                            onClick = { hid = JoyConDesc(ControllerType.LEFT_JOYCON) },
                            enabled = !isRegistered,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (hid is JoyConDesc && (hid as JoyConDesc).type == ControllerType.LEFT_JOYCON) Color.Cyan else Color.Gray
                            )
                        ) {
                            Text("L")
                        }
                        TextButton(
                            onClick = { hid = JoyConDesc(ControllerType.RIGHT_JOYCON) },
                            enabled = !isRegistered,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (hid is JoyConDesc && (hid as JoyConDesc).type == ControllerType.RIGHT_JOYCON) Color.Cyan else Color.Gray
                            )
                        ) {
                            Text("R")
                        }
                        TextButton(
                            onClick = { hid = JoyConDesc(ControllerType.PRO_CONTROLLER) },
                            enabled = !isRegistered,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (hid is JoyConDesc && (hid as JoyConDesc).type == ControllerType.PRO_CONTROLLER) Color.Cyan else Color.Gray
                            )
                        ) {
                            Text("Pro")
                        }
                    }
                }
            }

            StatusRow(stringResource(R.string.registration), if (isRegistered) stringResource(R.string.registered) else stringResource(R.string.not_registered), if (isRegistered) Color.Green else Color.Red)
            StatusRow(stringResource(R.string.host_connection), if (hostDevice != null) hostDevice?.name ?: stringResource(R.string.connected) else stringResource(R.string.disconnected), if (hostDevice != null) Color.Green else Color.Red)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    registerApp()
                    if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                        startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), 1)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegistered) stringResource(R.string.registered) else stringResource(R.string.register_btn_default))
            }


            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { currentScreen = AppScreen.Controller },
                modifier = Modifier.fillMaxWidth(),
                enabled = isRegistered,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text(stringResource(R.string.enter_controller_btn))
            }
        }

        if (showMacDialog) {
            var text by remember { mutableStateOf(bluetoothAddress) }
            AlertDialog(
                onDismissRequest = { showMacDialog = false },
                title = { Text(stringResource(R.string.set_bluetooth_mac)) },
                text = {
                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("00:11:22:33:44:55") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (isValidMac(text)) {
                            bluetoothAddress = text
                            getSharedPreferences("groove_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putString("bluetooth_mac", text)
                                .apply()
                            showMacDialog = false
                        } else {
                            Toast.makeText(this@MainActivity, R.string.mac_address_invalid, Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMacDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    private fun isValidMac(mac: String): Boolean {
        return Regex("^([0-9A-Fa-f]{2}[:]){5}([0-9A-Fa-f]{2})\$").matches(mac)
    }

    @Composable
    fun StatusRow(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.LightGray)
            Text(value, color = valueColor)
        }
    }

    @Composable
    fun GrooveCoaster() {
        var isTopRightPressed by remember { mutableStateOf(false) }
        var isBottomRightPressed by remember { mutableStateOf(false) }
        var exitProgress by remember { mutableStateOf(0f) }

        LaunchedEffect(isTopRightPressed, isBottomRightPressed) {
            if (isTopRightPressed && isBottomRightPressed) {
                val startTime = System.currentTimeMillis()
                val duration = 1500L
                while (true) {
                    val elapsed = System.currentTimeMillis() - startTime
                    exitProgress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                    if (elapsed >= duration) break
                    delay(16)
                }
                currentScreen = AppScreen.Setup
            } else {
                exitProgress = 0f
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val w = size.width
                        val h = size.height
                        val area = 100.dp.toPx()

                        isTopRightPressed = event.changes.any { it.pressed && it.position.x > w - area && it.position.y < area }
                        isBottomRightPressed = event.changes.any { it.pressed && it.position.x > w - area && it.position.y > h - area }
                    }
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Control(Modifier.fillMaxWidth().weight(1f)) { event ->
                    sendReport(event, if (Orientation == 0) ControlType.Left else ControlType.Right, "LEFT")
                }
                Divider(color = Color.White, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
                Control(Modifier.fillMaxWidth().weight(1f)) { event ->
                    sendReport(event, if (Orientation != 0) ControlType.Left else ControlType.Right, "RIGHT")
                }
            }

            // 退出提示和进度
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isTopRightPressed && isBottomRightPressed) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            progress = exitProgress,
                            color = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.exiting), color = Color.White, fontSize = 20.sp)
                    }
                } else if (isTopRightPressed || isBottomRightPressed) {
                    Text(
                        stringResource(R.string.exit_hint),
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            // 视觉提示：右上角
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        color = if (isTopRightPressed) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )

            // 视觉提示：右下角
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(
                        color = if (isBottomRightPressed) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
        }
    }

    private fun sendReport(event: ControlEvent, type: ControlType, tag: String) {
        val currentHid = hid ?: return
        val device = hidDevice ?: return
        val host = hostDevice

        if (currentHid is JoyConDesc) {
            if (host != null) {
                joyConController?.sendGrooveReport(host, event, type)
            }
            return
        }

        val report = currentHid.getReport(event, type)
        // Log.d(tag, "sendReport: ${report.map { it.toInt() }.joinToString()}")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // host 可以为 null，表示向所有已连接设备发送，但最好有明确的 host
        val success = device.sendReport(host, currentHid.reportId.toInt(), report)
        if (!success) {
            Log.e("HID", "Failed to send report")
        }
    }
}