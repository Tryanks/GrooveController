package com.tryanks.groovecontroller

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tryanks.groovecontroller.ui.theme.GrooveControllerTheme
import kotlinx.coroutines.coroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GrooveControllerTheme(true) {
                Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Control(Modifier.fillMaxWidth().weight(1f)) {
                            // TODO: Left event
                        }
                        Divider(color = Color.White, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
                        Control(Modifier.fillMaxWidth().weight(1f)) {
                            // TODO: Right event
                        }
                    }
                }
            }
        }
    }
}