package io.github.mossrf.dronewatch.core

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 纯地理计算工具，不依赖 Android / MSDK，便于在 JVM 单测里直接验证。
 */
object Geo {
    private const val EARTH_RADIUS_M = 6_371_000.0

    /**
     * 两个 WGS-84 经纬度之间的水平面距离（米），Haversine 公式。
     * 用于把「飞机坐标 + 起飞点(home)坐标」收敛成手表一屏能看的 distanceM。
     */
    fun horizontalDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(rLat1) * cos(rLat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }
}
