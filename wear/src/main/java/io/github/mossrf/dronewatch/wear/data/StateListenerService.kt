package io.github.mossrf.dronewatch.wear.data

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import io.github.mossrf.dronewatch.core.DroneState
import io.github.mossrf.dronewatch.core.WearProtocol

/**
 * 监听手机端经 DataClient 写入的遥测 DataItem。
 */
class StateListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED ||
                event.dataItem.uri.path != WearProtocol.PATH_STATE
            ) return@forEach

            val map = DataMapItem.fromDataItem(event.dataItem).dataMap
            val json = map.getString(WearProtocol.KEY_STATE_JSON) ?: return@forEach
            runCatching {
                val state = WearProtocol.json.decodeFromString(DroneState.serializer(), json)
                WearStateStore.update(state)
                Log.i(TAG, "state updated: connected=${state.connected} battery=${state.batteryPercent}")
            }.onFailure { Log.e(TAG, "decode state failed", it) }
        }
    }

    companion object {
        private const val TAG = "StateListener"
    }
}
