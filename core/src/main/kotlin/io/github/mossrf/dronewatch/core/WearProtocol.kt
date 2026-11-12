package io.github.mossrf.dronewatch.core

import kotlinx.serialization.json.Json

/**
 * 手机与手表两端共享的 Wear Data Layer 通道约定与 JSON 配置。
 */
object WearProtocol {
    /** MessageClient 路径：手表 → 手机，payload 为 [CommandEnvelope] 的 JSON */
    const val PATH_COMMAND = "/dronewatch/command"

    /** DataClient 路径：手机 → 手表，DataMap 内含 state_json 字段 */
    const val PATH_STATE = "/dronewatch/state"

    /** DataMap 中存放 [DroneState] JSON 的键 */
    const val KEY_STATE_JSON = "state_json"

    /** MessageClient 路径：手表 → 手机，请求立即推送一次最新状态 */
    const val PATH_STATE_REQUEST = "/dronewatch/state_request"

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
}
