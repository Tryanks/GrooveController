package com.tryanks.groovecontroller

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    private val btAdapter: BluetoothAdapter = BluetoothManager.getAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}