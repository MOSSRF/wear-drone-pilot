package io.github.mossrf.dronewatch.phone.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import io.github.mossrf.dronewatch.core.DroneDataSource
import io.github.mossrf.dronewatch.core.WearProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 手机 → 手表状态同步。
 * 用 DataClient 写入 DataItem，系统负责蓝牙转发与离线缓存，
 * 手表端通过 WearableListenerService / DataClient 监听接收。
 */
class WearSyncManager(
    private val context: Context,
    private val dataSource: DroneDataSource,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            dataSource.state
                .map { WearProtocol.json.encodeToString(io.github.mossrf.dronewatch.core.DroneState.serializer(), it) }
                .distinctUntilChanged()
                .collect { json -> pushState(json) }
        }
    }

    private fun pushState(stateJson: String) {
        val request = PutDataMapRequest.create(WearProtocol.PATH_STATE).apply {
            dataMap.putString(WearProtocol.KEY_STATE_JSON, stateJson)
            // Urgent 标识让状态更新即时跨设备投递
            setUrgent()
        }.asPutDataRequest()

        Wearable.getDataClient(context).putDataItem(request)
            .addOnSuccessListener { Log.i(TAG, "state pushed to wear") }
            .addOnFailureListener { e -> Log.w(TAG, "push state failed", e) }
    }

    companion object {
        private const val TAG = "WearSyncManager"
    }
}
