package io.github.mossrf.dronewatch.wear.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import io.github.mossrf.dronewatch.core.DroneCommand
import io.github.mossrf.dronewatch.core.DroneState
import io.github.mossrf.dronewatch.wear.data.WearCommandSender
import io.github.mossrf.dronewatch.wear.data.WearStateStore
import kotlinx.coroutines.launch

class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WearDroneScreen(WearCommandSender(applicationContext))
            }
        }
    }
}

@Composable
private fun WearDroneScreen(sender: WearCommandSender) {
    val state by WearStateStore.state.collectAsState()
    val listState = rememberScalingLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            state = listState,
        ) {
            item { StatusHeader(state) }
            item { MetricRow("电量", if (state.batteryPercent < 0) "—" else "${state.batteryPercent}%") }
            item { MetricRow("卫星", if (state.satellites < 0) "—" else state.satellites.toString()) }
            item { MetricRow("高度", "%.0f m".format(state.altitudeM)) }
            item { MetricRow("距离", "%.0f m".format(state.distanceM)) }
            item { MetricRow("速度", "%.1f m/s".format(state.speedMS)) }
            item {
                CommandChip("一键返航") {
                    scope.launch { sender.send(DroneCommand.RETURN_TO_HOME) }
                }
            }
            item {
                CommandChip("紧急悬停") {
                    scope.launch { sender.send(DroneCommand.EMERGENCY_BRAKE) }
                }
            }
            item {
                CommandChip("拍照") {
                    scope.launch { sender.send(DroneCommand.SHOOT_PHOTO) }
                }
            }
            item {
                CommandChip(if (state.recording) "停止录像" else "开始录像") {
                    scope.launch { sender.send(DroneCommand.TOGGLE_RECORD) }
                }
            }
        }
    }
}

@Composable
private fun StatusHeader(state: DroneState) {
    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(
            text = if (state.connected) "已连接 · ${state.model}" else "未连接",
            style = MaterialTheme.typography.title3,
        )
        Text(
            text = state.sdkStatus,
            style = MaterialTheme.typography.caption2,
        )
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Chip(
        onClick = {},
        enabled = false,
        colors = ChipDefaults.secondaryChipColors(),
        label = {
            Text(
                text = "$label  $value",
                style = MaterialTheme.typography.body2,
            )
        },
    )
}

@Composable
private fun CommandChip(label: String, onClick: () -> Unit) {
    Chip(
        onClick = onClick,
        colors = ChipDefaults.primaryChipColors(),
        label = { Text(label, style = MaterialTheme.typography.button) },
    )
}
