package io.github.mossrf.dronewatch.core

import kotlinx.serialization.Serializable

/**
 * 手表端 → 手机端的控制指令。
 * 第一期只放高安全等级动作；拍照/录像/跟随等放到后续迭代。
 */
@Serializable
enum class DroneCommand {
    /** 一键返航 */
    RETURN_TO_HOME,

    /** 紧急悬停 */
    EMERGENCY_BRAKE,

    /** 起飞（需手机端二次确认逻辑把关，暂只做通道预留） */
    TAKEOFF,

    /** 降落 */
    LAND,

    /** 拍照 */
    SHOOT_PHOTO,

    /** 录像开关 */
    TOGGLE_RECORD,
}

/** 指令信封，携带发送时间便于手机端做去重/过期判断 */
@Serializable
data class CommandEnvelope(
    val command: DroneCommand,
    val sentAt: Long,
)
