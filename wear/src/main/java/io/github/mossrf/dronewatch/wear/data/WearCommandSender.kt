package io.github.mossrf.dronewatch.wear.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import io.github.mossrf.dronewatch.core.CommandEnvelope
import io.github.mossrf.dronewatch.core.DroneCommand
import io.github.mossrf.dronewatch.core.WearProtocol
import kotlinx.coroutines.tasks.await

/**
 * 手表 → 手机指令发送。
 * MessageClient 需要先找到已配对的手机节点；找不到节点时返回 false，由 UI 提示。
 */
class WearCommandSender(private val context: Context) {

    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    suspend fun send(command: DroneCommand): Boolean {
        val nodeId = findPhoneNode() ?: run {
            Log.w(TAG, "no paired phone node found")
            return false
        }
        val payload = WearProtocol.json.encodeToString(
            CommandEnvelope.serializer(),
            CommandEnvelope(command, System.currentTimeMillis()),
        ).toByteArray(Charsets.UTF_8)

        return runCatching {
            messageClient.sendMessage(nodeId, WearProtocol.PATH_COMMAND, payload).await()
            true
        }.onFailure { Log.e(TAG, "send command failed", it) }.getOrDefault(false)
    }

    private suspend fun findPhoneNode(): String? {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        return nodes.firstOrNull()?.id
    }

    companion object {
        private const val TAG = "WearCommandSender"
    }
}
