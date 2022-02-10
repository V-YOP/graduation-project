package yukina.final_design_deprecated.util;

import java.math.BigDecimal;

// 转换经纬度的差为长度的类
public final class LatLont {
    private static final double EARTH_RADIUS = 6378.137;
    private LatLont(){}
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
    // 返回的单位为米
    public static double getDistance(BigDecimal lat1, BigDecimal lont1, BigDecimal lat2, BigDecimal lont2)
    {
        double radLat1 = rad(lat1.doubleValue());
        double radLat2 = rad(lat2.doubleValue());
        double a = radLat1 - radLat2;
        double b = rad(lont1.doubleValue()) - rad(lont2.doubleValue());

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        return s * EARTH_RADIUS * 1000;
    }
}
