package io.github.mossrf.dronewatch.core

import kotlinx.coroutines.flow.StateFlow

/**
 * 数据源抽象：MSDK 真机实现与仿真实现都实现这个接口，
 * UI/同步层不感知差异，方便无飞机时先打通手机↔手表链路。
 */
interface DroneDataSource {
    val state: StateFlow<DroneState>
    fun execute(command: DroneCommand)
}
