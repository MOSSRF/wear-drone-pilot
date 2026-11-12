package io.github.mossrf.dronewatch.phone.data

import io.github.mossrf.dronewatch.core.DroneCommand
import io.github.mossrf.dronewatch.core.DroneDataSource
import io.github.mossrf.dronewatch.core.DroneState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 手机端遥测数据源。
 *
 * 设计原则：本类不直接依赖 MSDK 的具体 Key，只暴露状态更新钩子；
 * MSDK KeyManager 的监听/动作封装在 [MsdkCommandExecutor] 和后续的
 * MsdkKeyWiring 中，避免 SDK 版本变动时牵动通信层。
 */
class MsdkDroneDataSource : DroneDataSource {

    private val _state = MutableStateFlow(DroneState.DISCONNECTED)
    override val state: StateFlow<DroneState> = _state.asStateFlow()

    /** 指令执行器，由 MsdkCommandExecutor 在 Application 中注入 */
    var commandExecutor: ((DroneCommand) -> Unit)? = null

    fun updateSdkStatus(status: String) {
        _state.value = _state.value.copy(sdkStatus = status, timestamp = now())
    }

    fun onProductChanged(productId: Int, connected: Boolean) {
        _state.value = _state.value.copy(
            connected = connected,
            model = if (connected) "PRODUCT_$productId" else "UNKNOWN",
            timestamp = now(),
        )
    }

    /** MSDK Key 监听回调统一走这里，只传变化字段 */
    fun patch(transform: DroneState.() -> DroneState) {
        _state.value = _state.value.transform().copy(timestamp = now())
    }

    override fun execute(command: DroneCommand) {
        val executor = commandExecutor
        if (executor == null) {
            updateSdkStatus("COMMAND_REJECTED: executor not ready")
            return
        }
        executor(command)
    }

    private fun now() = System.currentTimeMillis()
}
