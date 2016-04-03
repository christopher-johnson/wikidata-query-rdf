package org.wikidata.query.rdf.common;

import org.junit.runner.RunWith;
import org.wikidata.query.rdf.common.WikibasePoint.CoordinateOrder;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;

@RunWith(RandomizedRunner.class)
public class WikibasePointUnitTest extends RandomizedTest {

    @Test
    public void fromStringDefault() {
        pointFromString("POINT(12.34 56.98)", "12.34", "56.98", null);
    }

    @Test
    public void fromStringLatLong() {
        pointFromStringOrder("POINT(12.34 56.98)", "12.34", "56.98", null, CoordinateOrder.LAT_LONG);
    }

    @Test
    public void fromStringLongLat() {
        pointFromStringOrder("POINT(12.34 56.98)", "56.98", "12.34", null, CoordinateOrder.LONG_LAT);
    }

    @Test
    public void fromStringGlobe() {
        pointFromString("<On the Moon> POINT(12.34 56.98)", "12.34", "56.98", "On the Moon");
    }

    @Test
    public void fromStringGlobeLongLat() {
        pointFromStringOrder("<On Mars> POINT(12.34 56.98)", "56.98", "12.34", "On Mars", CoordinateOrder.LONG_LAT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badFormat() {
        pointFromString("Points(12.34,56.98)", "12.34", "56.98", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badFormat2() {
        pointFromString("On Mars> POINT(12.34 56.98)", "12.34", "56.98", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badFormat3() {
        pointFromString("<On Mars>POINT(12.34 56.98)", "12.34", "56.98", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badOnlyOne() {
        pointFromString("<On Mars> POINT(12.34)", "12.34", "56.98", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badThree() {
        pointFromString("<On Mars> POINT(12.34 5.6 7.8)", "12.34", "56.98", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badUrl() {
        pointFromString("<On the Moon POINT(12.34 56.98)", "12.34", "56.98", "On the Moon");
    }

    private void pointFromString(String s, String lat, String lon, String globe) {
        WikibasePoint p = new WikibasePoint(s);
        assertEquals(p.getLatitude(), lat);
        assertEquals(p.getLongitude(), lon);
        assertEquals(p.getGlobe(), globe);
    }

    private void pointFromStringOrder(String s, String lat, String lon, String globe, CoordinateOrder order) {
        WikibasePoint p = new WikibasePoint(s, order);
        assertEquals(p.getLatitude(), lat);
        assertEquals(p.getLongitude(), lon);
        assertEquals(p.getGlobe(), globe);
    }

    @Test
    public void roundtripTests() {
        roundtrip("POINT(12.34 56.98)");
     //   roundtrip("<http://what?> POINT(12.34 56)");
     //   roundtrip("<not even url> POINT(12 56.98)");
    }

    private void roundtrip(String s) {
        WikibasePoint p = new WikibasePoint(s);
        assertEquals(s, p.toString());
    }

    @Test
    public void fromComps() {
        pointFromCompsOrder("POINT(12.34 56.98)", "12.34", "56.98", null, CoordinateOrder.LAT_LONG);
        pointFromCompsOrder("POINT(56.98 12.34)", "12.34", "56.98", null, CoordinateOrder.LONG_LAT);
 //       pointFromCompsOrder("<On the Moon> POINT(12.34 56.98)", "12.34", "56.98", "On the Moon", CoordinateOrder.LAT_LONG);
  //      pointFromCompsOrder("<http://mars> POINT(56.98 12.34)", "12.34", "56.98", "http://mars", CoordinateOrder.LONG_LAT);
    }

    private void pointFromCompsOrder(String s, String lat, String lon, String globe, CoordinateOrder order) {
        WikibasePoint p = new WikibasePoint(new String[] {lat, lon}, globe, CoordinateOrder.LAT_LONG);
        assertEquals(s, p.toOrder(order));
    }
}
