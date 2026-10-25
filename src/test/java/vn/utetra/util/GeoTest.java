package vn.utetra.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoTest {

    @Test
    void samepointIsZero() {
        assertEquals(0.0, Geo.km(10.77, 106.70, 10.77, 106.70), 1e-9);
    }

    @Test
    void knownDistanceIsReasonable() {
        // Quận 1 -> Thủ Đức: khoảng 9-12 km đường chim bay
        double km = Geo.km(10.7743, 106.7040, 10.8505, 106.7717);
        assertTrue(km > 8 && km < 13, "km = " + km);
    }

    @Test
    void validatesRanges() {
        assertTrue(Geo.validLat(10.0));
        assertFalse(Geo.validLat(91.0));
        assertFalse(Geo.validLat(null));
        assertTrue(Geo.validLon(-180.0));
        assertFalse(Geo.validLon(181.0));
    }
}
