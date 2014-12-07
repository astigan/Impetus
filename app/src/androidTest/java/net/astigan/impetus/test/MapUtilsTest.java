package net.astigan.impetus.test;

import android.test.InstrumentationTestCase;

import com.google.android.gms.maps.model.LatLng;

import net.astigan.impetus.utils.MapUtils;

/**
 * A set of unit tests for testing map util functions. As most methods use some sort of random
 * number generation, each method is called repeatedly and tested on grounds of probability
 */

public class MapUtilsTest extends InstrumentationTestCase {

    public void testGetRandomMetres() {

        double randomMetres;

        for (int i=0; i<50; i++) {
            for (int k=1000; k<50000; k+=1000) {

                randomMetres = MapUtils.getRandomMetres(k);

                double lowest = k - ((k / 100) * 30);
                double highest = k + ((k / 100) * 30);

                if (randomMetres < lowest || randomMetres > highest) {
                    assertEquals("Fail: ", Double.toString(randomMetres));
                }
            }
        }
    }

    public void testGetRandomDirection() {

        int direction;

        boolean limitsReached = false;

        for (int i=0; i<10000; i++) {
            direction = MapUtils.getRandomJourneyDirection();

            if (direction < 0 || direction > 90) {
                assertEquals("Fail: ", Integer.toString(direction));
            }

            if (direction == 0 || direction == 90) {
                limitsReached = true;
            }
        }
        assertEquals(true, limitsReached);
    }

    public void testGetSanitisedPosition() {

        // correct for values that exceed the bounds of lat/lng e.g. 92.5 & 181.35

        LatLng result;

        // input requires no sanitisation for these cases
        result = MapUtils.getSanitisedPosition(0.000, 0.000);
        assertEquals(new LatLng(0.000, 0.000), result);

        result = MapUtils.getSanitisedPosition(0.000, 25.000);
        assertEquals(new LatLng(0.000, 25.000), result);

        result = MapUtils.getSanitisedPosition(-25.000, 0.000);
        assertEquals(new LatLng(-25.000, 0.000), result);

        result = MapUtils.getSanitisedPosition(45.000, -45.000);
        assertEquals(new LatLng(45.000, -45.000), result);

        result = MapUtils.getSanitisedPosition(90.000, 180.000);
        assertEquals(new LatLng(90.000, 180.000), result);

        result = MapUtils.getSanitisedPosition(-90.000, -180.000);
        assertEquals(new LatLng(-90.000, -180.000), result);

        result = MapUtils.getSanitisedPosition(-0.250, -0.350);
        assertEquals(new LatLng(-0.250, -0.350), result);


        // input requires sanitisation for these cases
        result = MapUtils.getSanitisedPosition(92.000, 180.500);
        assertEquals(new LatLng(88.000, -179.500), result);

        result = MapUtils.getSanitisedPosition(-90.750, -180.500);
        assertEquals(new LatLng(-89.250, 179.500), result);

        result = MapUtils.getSanitisedPosition(-90.150, -180.500);
        assertEquals(new LatLng(-89.850, 179.500), result);

    }

    // TODO further MapsUtils testing


}
