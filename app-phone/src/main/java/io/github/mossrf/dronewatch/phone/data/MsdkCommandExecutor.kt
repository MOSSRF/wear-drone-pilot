package io.github.mossrf.dronewatch.phone.data

import android.util.Log
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.key.DJIKey
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.common.error.IDJIError
import dji.v5.et.action
import dji.v5.et.create
import io.github.mossrf.dronewatch.core.DroneCommand

/**
 * MSDK V5.20.1 指令执行封装。
 *
 * 所有 Key 名与调用范式均来自官方 sample（dev-sdk-main）可编译源码与官方 API 文档核对，
 * 非记忆/臆测。调用统一走官方 ET 扩展链 `DJIActionKeyInfo.create().action{}`：
 *  - 扩展签名（javap dji.v5.et.DJIKeyInfoEtKt / DJIKeyEtKt 实证）：
 *      create(DJIActionKeyInfo<T,R>) / create(..., ComponentIndexType) -> DJIKey.ActionKey<T,R>
 *      ActionKey.action(onSuccess: (R)->Unit, onFailure: (IDJIError)->Unit)
 *  - 起飞 KeyStartTakeoff / 降落 KeyStartAutoLanding：sample BasicAircraftControlVM
 *  - 返航 KeyStartGoHome：sample ReturnHomeWidgetModel
 *  - 拍照 KeyStartShootPhoto（相机组件必须带索引）：sample patrol/CaptureController
 *  - 录像 KeyStartRecord / KeyStopRecord：官方 CameraKey 文档 + 同套 ET 范式
 *
 * 紧急刹车（EMERGENCY_BRAKE）：V5.20.1 的 FlightControllerKey 全量枚举中
 * 不存在 Brake/Emergency action（官方文档 "brake" 仅出现在悬停描述文字里）。
 * 在 SDK 提供对应能力前，这里保持诚实桩：明确上报 UNSUPPORTED 并返回未受理，
 * 绝不伪造一个不存在的 Key。
 */
class MsdkCommandExecutor(
    private val dataSource: MsdkDroneDataSource,
) {

    /** 受理并下发动作。返回 true 表示已交给 MSDK（不代表飞机一定执行成功）。 */
    fun execute(command: DroneCommand): Boolean {
        Log.i(TAG, "execute command: $command")
        return when (command) {
            DroneCommand.TAKEOFF ->
                runAction(FlightControllerKey.KeyStartTakeoff.create(), "takeoff")

            DroneCommand.LAND ->
                runAction(FlightControllerKey.KeyStartAutoLanding.create(), "landing")

            DroneCommand.RETURN_TO_HOME ->
                runAction(FlightControllerKey.KeyStartGoHome.create(), "return-to-home")

            DroneCommand.SHOOT_PHOTO ->
                runAction(
                    CameraKey.KeyStartShootPhoto.create(CAMERA_INDEX),
                    "shoot-photo",
                )

            DroneCommand.TOGGLE_RECORD -> {
                // 以当前遥测里的录像态决定开始还是停止，避免手表端需要维护两套指令
                val start = !dataSource.state.value.recording
                val actionKey = if (start) {
                    CameraKey.KeyStartRecord.create(CAMERA_INDEX)
                } else {
                    CameraKey.KeyStopRecord.create(CAMERA_INDEX)
                }
                runAction(actionKey, if (start) "record-start" else "record-stop")
            }

            DroneCommand.EMERGENCY_BRAKE -> {
                // SDK 无对应 Key：诚实上报，不编造 API。
                Log.w(TAG, "emergency brake requested but MSDK V5.20.1 exposes no brake action")
                dataSource.updateSdkStatus("UNSUPPORTED: emergency-brake (no MSDK key)")
                false
            }
        }
    }

    /**
     * 统一动作下发：成功/失败都回写 sdkStatus，方便手表端与日志排障。
     * 显式泛型 R 对齐官方扩展 `ActionKey<*, R>.action(onSuccess: (R)->Unit, onFailure)`，
     * 兼容各动作 Key 不同的结果泛型（本项目只关心成功/失败，不消费结果体）。
     */
    private fun <R> runAction(key: DJIKey.ActionKey<*, R>, label: String): Boolean {
        return try {
            key.action(
                { _: R ->
                    Log.i(TAG, "$label accepted by MSDK")
                    dataSource.updateSdkStatus("ACTION_OK: $label")
                },
                { error: IDJIError ->
                    Log.e(TAG, "$label failed: ${error.errorCode()} ${error.description()}")
                    dataSource.updateSdkStatus("ACTION_FAIL: $label ${error.errorCode()}")
                },
            )
            true
        } catch (t: Throwable) {
            // KeyManager 在未注册/未连接时可能同步抛异常，不能让它崩到通信服务
            Log.e(TAG, "$label dispatch threw", t)
            dataSource.updateSdkStatus("ACTION_ERROR: $label ${t.javaClass.simpleName}")
            false
        }
    }

    companion object {
        private const val TAG = "MsdkCommandExecutor"

        /** 单云台/主摄机型统一用左或主组件索引，与 sample CaptureController 保持一致。 */
        private val CAMERA_INDEX = ComponentIndexType.LEFT_OR_MAIN
    }
}
