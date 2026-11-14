package io.github.mossrf.dronewatch.phone.data

import android.util.Log
import dji.sdk.keyvalue.key.AirLinkKey
import dji.sdk.keyvalue.key.BatteryKey
import dji.sdk.keyvalue.key.CameraKey
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.sdk.keyvalue.value.common.LocationCoordinate2D
import dji.sdk.keyvalue.value.common.LocationCoordinate3D
import dji.sdk.keyvalue.value.common.Velocity3D
import dji.sdk.keyvalue.value.flightcontroller.LowBatteryRTHInfo
import dji.v5.manager.KeyManager
import io.github.mossrf.dronewatch.core.Geo
import kotlin.math.sqrt

/**
 * MSDK V5.20.1 遥测监听：把分散的 KeyManager 推送收敛成 [MsdkDroneDataSource] 的单一快照。
 *
 * 所有 Key 与值类型均经官方 sample 源码 / API 文档核对：
 *  - BatteryKey.KeyChargeRemainingInPercent : Int（带电池组件索引）
 *  - FlightControllerKey.KeyGPSSatelliteCount : Int
 *  - FlightControllerKey.KeyAltitude : Double（相对起飞点高度，米）
 *  - FlightControllerKey.KeyAircraftVelocity : Velocity3D(x/y/z: Double)
 *  - FlightControllerKey.KeyAircraftLocation3D : LocationCoordinate3D
 *  - FlightControllerKey.KeyHomeLocation : LocationCoordinate2D
 *  - FlightControllerKey.KeyLowBatteryRTHInfo : LowBatteryRTHInfo(remainingFlightTime)
 *  - AirLinkKey.KeyDownLinkQualityRaw : Int（sample VideoSignalWidgetModel）
 *  - CameraKey.KeyIsRecording / KeyConnection : Boolean（带相机组件索引）
 *
 * 监听范式对齐 sample SdkAutoFlightGateway：KeyManager.listen(key, holder){}，
 * 注销统一 KeyManager.cancelListen(holder)。
 */
class MsdkTelemetryMonitor(
    private val dataSource: MsdkDroneDataSource,
) {
    // 所有监听共用一个持有者，stop() 时一次性注销，避免泄漏
    private val listenHolder = Any()

    @Volatile
    private var started = false

    // 距离需要飞机坐标与 home 坐标两者，缓存最新值，任一更新即重算
    @Volatile
    private var aircraft: LocationCoordinate3D? = null

    @Volatile
    private var home: LocationCoordinate2D? = null

    fun start() {
        if (started) return
        started = true
        Log.i(TAG, "telemetry monitor start")
        val km = KeyManager.getInstance()

        // ---- 飞控连接 / 卫星 / 高度 / 速度 / 位置 / 续航 ----
        listen(km, KeyTools.createKey(FlightControllerKey.KeyConnection)) { connected: Boolean ->
            dataSource.patch { copy(connected = connected) }
        }
        listen(km, KeyTools.createKey(FlightControllerKey.KeyGPSSatelliteCount)) { count: Int ->
            dataSource.patch { copy(satellites = count) }
        }
        listen(km, KeyTools.createKey(FlightControllerKey.KeyAltitude)) { altitude: Double ->
            dataSource.patch { copy(altitudeM = altitude.toFloat()) }
        }
        listen(km, KeyTools.createKey(FlightControllerKey.KeyAircraftVelocity)) { v: Velocity3D ->
            // 对地（水平）速度，忽略垂直分量 z
            val ground = sqrt(v.x * v.x + v.y * v.y)
            dataSource.patch { copy(speedMS = ground.toFloat()) }
        }
        listen(km, KeyTools.createKey(FlightControllerKey.KeyAircraftLocation3D)) { loc: LocationCoordinate3D ->
            aircraft = loc
            recomputeDistance()
        }
        listen(km, KeyTools.createKey(FlightControllerKey.KeyHomeLocation)) { loc: LocationCoordinate2D ->
            home = loc
            recomputeDistance()
        }
        listen(km, KeyTools.createKey(FlightControllerKey.KeyLowBatteryRTHInfo)) { info: LowBatteryRTHInfo ->
            dataSource.patch { copy(remainingFlightTimeS = info.remainingFlightTime) }
        }

        // ---- 电池（主电池组件索引）----
        listen(
            km,
            KeyTools.createKey(BatteryKey.KeyChargeRemainingInPercent, ComponentIndexType.LEFT_OR_MAIN),
        ) { percent: Int ->
            dataSource.patch { copy(batteryPercent = percent) }
        }

        // ---- 图传信号（原始质量，收敛到 0..100）----
        listen(km, KeyTools.createKey(AirLinkKey.KeyDownLinkQualityRaw)) { raw: Int ->
            dataSource.patch { copy(signalQuality = raw.coerceIn(0, 100)) }
        }

        // ---- 相机（主摄组件索引）----
        listen(
            km,
            KeyTools.createKey(CameraKey.KeyConnection, ComponentIndexType.LEFT_OR_MAIN),
        ) { connected: Boolean ->
            dataSource.patch { copy(cameraAvailable = connected) }
        }
        listen(
            km,
            KeyTools.createKey(CameraKey.KeyIsRecording, ComponentIndexType.LEFT_OR_MAIN),
        ) { recording: Boolean ->
            dataSource.patch { copy(recording = recording) }
        }
    }

    fun stop() {
        if (!started) return
        started = false
        runCatching { KeyManager.getInstance().cancelListen(listenHolder) }
            .onFailure { Log.w(TAG, "cancelListen failed", it) }
        Log.i(TAG, "telemetry monitor stop")
    }

    private fun recomputeDistance() {
        val a = aircraft ?: return
        val h = home ?: return
        val meters = Geo.horizontalDistanceMeters(
            lat1 = a.latitude,
            lon1 = a.longitude,
            lat2 = h.latitude,
            lon2 = h.longitude,
        )
        dataSource.patch { copy(distanceM = meters.toFloat()) }
    }

    /**
     * 统一监听包装：类型在调用处由 lambda 形参推断；
     * 每个回调单独 runCatching，单个 Key 解析异常不影响其它链路。
     */
    private inline fun <reified T> listen(
        km: KeyManager,
        key: dji.sdk.keyvalue.key.DJIKey<T>,
        crossinline onValue: (T) -> Unit,
    ) {
        runCatching {
            km.listen(key, listenHolder) { _, newValue ->
                if (newValue is T) {
                    runCatching { onValue(newValue) }
                        .onFailure { Log.e(TAG, "telemetry handler failed for $key", it) }
                }
            }
        }.onFailure { Log.e(TAG, "listen failed for $key", it) }
    }

    companion object {
        private const val TAG = "MsdkTelemetry"
    }
}
