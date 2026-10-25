package vn.utetra.util;

/** Tính khoảng cách đường chim bay giữa hai toạ độ (công thức haversine). */
public final class Geo {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private Geo() { }

    public static double km(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    public static boolean validLat(Double v) { return v != null && v >= -90 && v <= 90; }
    public static boolean validLon(Double v) { return v != null && v >= -180 && v <= 180; }
}
