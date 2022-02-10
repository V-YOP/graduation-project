package yukina.final_design.util

import java.math.BigDecimal

// 转换经纬度的差为长度的类
private const val EARTH_RADIUS = 6378.137
private fun rad(d: Double): Double {
    return d * Math.PI / 180.0
}

// 返回的单位为米
fun getDistance(lat1: BigDecimal, lont1: BigDecimal, lat2: BigDecimal, lont2: BigDecimal): Double {
    val radLat1 = rad(lat1.toDouble())
    val radLat2 = rad(lat2.toDouble())
    val a = radLat1 - radLat2
    val b = rad(lont1.toDouble()) - rad(lont2.toDouble())
    val s = 2 * Math.asin(
        Math.sqrt(
            Math.pow(Math.sin(a / 2), 2.0) +
                    Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2.0)
        )
    )
    return s * EARTH_RADIUS * 1000
}
