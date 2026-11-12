package io.github.mossrf.dronewatch.phone

import android.app.Application
import android.util.Log
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.common.register.InitSettings
import dji.v5.common.register.SDKLinKMode
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback
import io.github.mossrf.dronewatch.core.DroneState
import io.github.mossrf.dronewatch.phone.data.MsdkCommandExecutor
import io.github.mossrf.dronewatch.phone.data.MsdkDroneDataSource
import io.github.mossrf.dronewatch.phone.wear.WearSyncManager

/**
 * MSDK 初始化入口。
 * 流程严格按官方 sample MSDKManagerVM：
 * init() → onInitProcess(INITIALIZE_COMPLETE) → registerApp() → onRegisterSuccess/Failure
 */
class WearDroneApp : Application() {

    lateinit var dataSource: MsdkDroneDataSource
        private set
    lateinit var wearSync: WearSyncManager
        private set

    override fun onCreate() {
        super.onCreate()
        dataSource = MsdkDroneDataSource()
        val commandExecutor = MsdkCommandExecutor(dataSource)
        dataSource.commandExecutor = { command -> commandExecutor.execute(command) }
        wearSync = WearSyncManager(this, dataSource)
        wearSync.start()
        initMsdk()
    }

    private fun initMsdk() {
        SDKManager.getInstance().init(this, InitSettings(SDKLinKMode.LOCAL), object : SDKManagerCallback {
            override fun onRegisterSuccess() {
                Log.i(TAG, "MSDK register success")
                dataSource.updateSdkStatus("REGISTERED")
            }

            override fun onRegisterFailure(error: IDJIError) {
                Log.e(TAG, "MSDK register failure: $error")
                // 用 toString() 规避不同 MSDK 版本 IDJIError 方法名差异
                dataSource.updateSdkStatus("REGISTER_FAILED: $error")
            }

            override fun onProductConnect(productId: Int) {
                Log.i(TAG, "product connected: $productId")
                dataSource.onProductChanged(productId, connected = true)
            }

            override fun onProductDisconnect(productId: Int) {
                Log.i(TAG, "product disconnected: $productId")
                dataSource.onProductChanged(productId, connected = false)
            }

            override fun onProductChanged(productId: Int) {
                Log.i(TAG, "product changed: $productId")
            }

            override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                Log.i(TAG, "init process: $event ($totalProcess%)")
                dataSource.updateSdkStatus("INIT:$event")
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    SDKManager.getInstance().registerApp()
                }
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                Log.i(TAG, "db download: $current/$total")
            }
        })
    }

    companion object {
        private const val TAG = "WearDroneApp"
        val INITIAL_STATE: DroneState = DroneState.DISCONNECTED
    }
}
