package io.github.mossrf.dronewatch.phone.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import io.github.mossrf.dronewatch.core.CommandEnvelope
import io.github.mossrf.dronewatch.core.DroneCommand
import io.github.mossrf.dronewatch.core.WearProtocol
import io.github.mossrf.dronewatch.phone.WearDroneApp

/**
 * 手表 → 手机指令接收入口。
 * 在 Manifest 中注册了 /dronewatch 路径前缀的 MESSAGE_RECEIVED 过滤器。
 */
class CommandListenerService : WearableListenerService() {

    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            WearProtocol.PATH_COMMAND -> handleCommand(event.data)
            WearProtocol.PATH_STATE_REQUEST -> Log.i(TAG, "wear requests immediate state")
            else -> Log.w(TAG, "unknown path: ${event.path}")
        }
    }

    private fun handleCommand(payload: ByteArray) {
        runCatching {
            val envelope = WearProtocol.json.decodeFromString(
                CommandEnvelope.serializer(),
                String(payload, Charsets.UTF_8),
            )
            Log.i(TAG, "command received: ${envelope.command}")
            // 安全闸门：未知/未来新增枚举值直接拒绝，防止老版本误执行
            if (envelope.command in DroneCommand.entries) {
                (application as WearDroneApp).dataSource.execute(envelope.command)
            }
        }.onFailure { Log.e(TAG, "bad command payload", it) }
    }

    companion object {
        private const val TAG = "CommandListener"
    }
}
