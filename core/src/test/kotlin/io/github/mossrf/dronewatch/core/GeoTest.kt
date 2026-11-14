package io.github.mossrf.dronewatch.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTest {

    @Test
    fun `same point has zero distance`() {
        val d = Geo.horizontalDistanceMeters(31.2304, 121.4737, 31.2304, 121.4737)
        assertEquals(0.0, d, 0.0)
    }

    @Test
    fun `one degree of longitude at equator is about 111 kilometers`() {
        // 赤道上经度差 1° ≈ 111.32 km，给较宽容差容忍地球半径取整
        val d = Geo.horizontalDistanceMeters(0.0, 0.0, 0.0, 1.0)
        assertEquals(111_320.0, d, 500.0)
    }

    @Test
    fun `distance is symmetric`() {
        val a = Geo.horizontalDistanceMeters(31.23, 121.47, 39.90, 116.40)
        val b = Geo.horizontalDistanceMeters(39.90, 116.40, 31.23, 121.47)
        assertEquals(a, b, 1e-6)
        // 上海到北京直线距离约 1000+ km，做个合理性下限校验
        assertTrue("expected >1000km, got $a", a > 1_000_000.0)
    }

    @Test
    fun `short baseline yields meter-scale result`() {
        // 纬度方向约 1e-5 度 ≈ 1.11 米
        val d = Geo.horizontalDistanceMeters(31.23040, 121.47370, 31.23041, 121.47370)
        assertEquals(1.11, d, 0.1)
    }
}
