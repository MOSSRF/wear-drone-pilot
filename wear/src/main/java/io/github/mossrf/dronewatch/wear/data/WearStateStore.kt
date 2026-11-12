package io.github.mossrf.dronewatch.wear.data

import io.github.mossrf.dronewatch.core.DroneState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 手表端全局状态持有者（单例）。
 * Service 收到 DataItem 更新后写入，Compose 界面订阅展示。
 */
object WearStateStore {
    private val _state = MutableStateFlow(DroneState.DISCONNECTED)
    val state: StateFlow<DroneState> = _state.asStateFlow()

    fun update(state: DroneState) {
        _state.value = state
    }
}
