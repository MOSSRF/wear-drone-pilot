package io.github.mossrf.dronewatch.phone.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mossrf.dronewatch.core.DroneState
import io.github.mossrf.dronewatch.phone.WearDroneApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataSource = (application as WearDroneApp).dataSource
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val state by dataSource.state.collectAsState()
                    PhoneStatusScreen(state)
                }
            }
        }
    }
}

@Composable
private fun PhoneStatusScreen(state: DroneState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("WearDrone Pilot（手机端）", style = MaterialTheme.typography.headlineSmall)
        Text("MSDK 状态：${state.sdkStatus}")
        Text("飞机连接：${if (state.connected) "已连接 (${state.model})" else "未连接"}")
        Text("电量：${if (state.batteryPercent < 0) "—" else "${state.batteryPercent}%"}")
        Text("卫星：${if (state.satellites < 0) "—" else state.satellites}")
        Text("高度：${"%.1f".format(state.altitudeM)} m")
        Text("距离：${"%.1f".format(state.distanceM)} m")
        Text("速度：${"%.1f".format(state.speedMS)} m/s")
        Text("信号：${if (state.signalQuality < 0) "—" else "${state.signalQuality}%"}")
        Text(
            "保持本应用在后台运行，手表即可通过 Wear Data Layer 接收状态并下发指令。",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
