package com.example.visitsubsystem.geo;

/**
 * Great-circle distance on a spherical Earth.
 *
 * <p>Uses the IUGG mean Earth radius (6,371,008.8 m). That is accurate to roughly
 * 0.5% versus the WGS84 ellipsoid — far inside the tolerances this app needs
 * (a 500 m compliance radius).</p>
 */
public final class Haversine {

    public static final double EARTH_RADIUS_METERS = 6_371_008.8;

    private Haversine() {
    }

    /**
     * Distance in meters between two WGS84 points given in decimal degrees.
     */
    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        requireValidLatitude(lat1);
        requireValidLatitude(lat2);
        requireValidLongitude(lng1);
        requireValidLongitude(lng2);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double sinLat = Math.sin(deltaLat / 2);
        double sinLng = Math.sin(deltaLng / 2);
        double a = sinLat * sinLat + Math.cos(radLat1) * Math.cos(radLat2) * sinLng * sinLng;

        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    private static void requireValidLatitude(double lat) {
        if (!Double.isFinite(lat) || lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("Latitude out of range [-90, 90]: " + lat);
        }
    }

    private static void requireValidLongitude(double lng) {
        if (!Double.isFinite(lng) || lng < -180.0 || lng > 180.0) {
            throw new IllegalArgumentException("Longitude out of range [-180, 180]: " + lng);
        }
    }
}
