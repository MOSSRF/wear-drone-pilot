package io.github.mossrf.dronewatch.phone.data

import android.util.Log
import io.github.mossrf.dronewatch.core.DroneCommand

/**
 * MSDK 指令执行封装（骨架）。
 *
 * 真机实现时用 KeyManager 动作接口，官方示例参考：
 *   SampleCode-V5/android-sdk-v5-sample/.../keyvalue/KeyOperatorCommand.kt
 * 典型形态（以返航为例，具体 Key 类名以 5.20.1 文档为准）：
 *
 *   KeyManager.getInstance().performAction(
 *       KeyTools.createKey(FlightControlKey.Key_START_RETURN_HOME),
 *       callback
 *   )
 *
 * 第一期先保证通道打通；接入真机时逐个填充 [map]，未支持指令返回 false。
 */
class MsdkCommandExecutor(
    private val dataSource: MsdkDroneDataSource,
) {

    /** 返回 true 表示已受理 */
    fun execute(command: DroneCommand): Boolean {
        Log.i(TAG, "execute command: $command")
        return when (command) {
            DroneCommand.RETURN_TO_HOME -> TODO_MSDK("FlightControlKey.Key_START_RETURN_HOME")
            DroneCommand.EMERGENCY_BRAKE -> TODO_MSDK("FlightControlKey.Key_BRAKE")
            DroneCommand.TAKEOFF -> TODO_MSDK("FlightControlKey.Key_TAKEOFF")
            DroneCommand.LAND -> TODO_MSDK("FlightControlKey.Key_LANDING")
            DroneCommand.SHOOT_PHOTO -> TODO_MSDK("CameraKey.Key_START_SHOOT_PHOTO")
            DroneCommand.TOGGLE_RECORD -> TODO_MSDK("CameraKey.Key_START_RECORD / Key_STOP_RECORD")
        }
    }

    private fun TODO_MSDK(keyHint: String): Boolean {
        // TODO(msdk): 接入 KeyManager.performAction，完成后删除此桩
        Log.w(TAG, "MSDK action not wired yet -> $keyHint")
        dataSource.updateSdkStatus("TODO_MSDK: $keyHint")
        return false
    }

    companion object {
        private const val TAG = "MsdkCommandExecutor"
    }
}
