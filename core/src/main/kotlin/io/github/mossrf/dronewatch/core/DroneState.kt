package io.github.mossrf.dronewatch.core

import kotlinx.serialization.Serializable

/**
 * 手机端 → 手表端的统一遥测快照。
 * 字段只保留手表一屏能看完的关键信息；MSDK 适配层负责把机型差异收敛到这里。
 */
@Serializable
data class DroneState(
    /** 数据产生时间（手机端 System.currentTimeMillis()） */
    val timestamp: Long = 0L,
    /** 飞机是否已通过 MSDK 连接 */
    val connected: Boolean = false,
    /** MSDK 上报的机型标识，未连接时为 UNKNOWN */
    val model: String = "UNKNOWN",
    /** MSDK 注册/初始化状态描述，用于排障 */
    val sdkStatus: String = "NOT_INITIALIZED",
    /** 电池剩余百分比 0..100，未知为 -1 */
    val batteryPercent: Int = -1,
    /** 可搜星/定位卫星数，未知为 -1 */
    val satellites: Int = -1,
    /** 相对起飞点高度，米 */
    val altitudeM: Float = 0f,
    /** 相对起飞点水平距离，米 */
    val distanceM: Float = 0f,
    /** 对地速度，米/秒 */
    val speedMS: Float = 0f,
    /** 图传信号质量 0..100，未知为 -1 */
    val signalQuality: Int = -1,
    /** 剩余续航估算，秒，未知为 -1 */
    val remainingFlightTimeS: Int = -1,
    /** 是否在录像 */
    val recording: Boolean = false,
    /** 相机是否就绪 */
    val cameraAvailable: Boolean = false,
) {
    companion object {
        val DISCONNECTED = DroneState()
    }
}
