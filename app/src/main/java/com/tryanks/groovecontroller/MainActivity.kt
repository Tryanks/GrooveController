package com.tryanks.groovecontroller

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
import com.tryanks.groovecontroller.ui.theme.GrooveControllerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GrooveCoaster()
        }
    }
}

@Composable
fun GrooveCoaster() {
    var hid by remember { mutableStateOf<BasicDescriptor?>(null) }

    hid = KeyboardDesc()

    GrooveControllerTheme(true) {
        Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Control(Modifier.fillMaxWidth().weight(1f)) {
                    val report = hid!!.getReport(it, if (Orientation == 0) ControlType.Left else ControlType.Right)
                    Log.d("LEFT", "controlView: ${report.map { it.toInt() }.joinToString()}")
                }
                Divider(color = Color.White, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
                Control(Modifier.fillMaxWidth().weight(1f)) {
                    val report = hid!!.getReport(it, if (Orientation != 0) ControlType.Left else ControlType.Right)
                    Log.d("RIGHT", "controlView: $report.map { it.toInt() }.joinToString()}")
                }
            }
        }
    }
}