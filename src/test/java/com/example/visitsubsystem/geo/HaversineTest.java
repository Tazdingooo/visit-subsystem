package com.example.visitsubsystem.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HaversineTest {

    @Test
    void samePointHasZeroDistance() {
        assertEquals(0.0, Haversine.distanceMeters(39.908692, 116.397477, 39.908692, 116.397477), 0.0);
    }

    @Test
    void oneDegreeLatitudeAtEquatorIsAbout111Point2Kilometers() {
        assertEquals(111_195.08, Haversine.distanceMeters(0, 0, 1, 0), 0.1);
    }

    @Test
    void oneDegreeLongitudeAtLatitude60IsAboutHalfThatDistance() {
        assertEquals(55_597.01, Haversine.distanceMeters(60, 0, 60, 1), 0.1);
    }

    @Test
    void knownParisToLondonDistance() {
        assertEquals(343_556.5, Haversine.distanceMeters(48.8566, 2.3522, 51.5074, -0.1278), 1.0);
    }

    @Test
    void handlesAntimeridian() {
        assertEquals(222_390.16, Haversine.distanceMeters(0, 179, 0, -179), 0.1);
    }

    @Test
    void rejectsOutOfRangeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> Haversine.distanceMeters(90.1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Haversine.distanceMeters(0, 0, 0, 180.1));
    }

    @Test
    void rejectsNaNCoordinatesInsteadOfReturningNaNDistance() {
        assertThrows(IllegalArgumentException.class, () -> Haversine.distanceMeters(Double.NaN, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Haversine.distanceMeters(0, Double.NaN, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Haversine.distanceMeters(0, 0, 0, Double.NaN));
    }

    @Test
    void rejectsInfiniteCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> Haversine.distanceMeters(Double.POSITIVE_INFINITY, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Haversine.distanceMeters(0, Double.NEGATIVE_INFINITY, 0, 0));
    }
}
