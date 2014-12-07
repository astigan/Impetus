package net.astigan.impetus.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 * A utilities class that generates a random destination for the user to travel to.
 */
public class MapUtils {

    private static final int EARTH_RADIUS = 6378137; // m
    private static final int DELTA_DISTANCE_FACTOR = 30; // % deviation

    /**
     * Generates random coordinates based on the approximate distance of desired travel (+/- 30%),
     * and the user's initial location. The random coordinates can be in any direction on the earth.
     * A summary of the algorithm is below:
     * <li>
     *     <ul>
     * 1) A random number of metres is obtained (+/- 30%).
     *     </ul>
     *     <ul>
     * 2) A random angle of direction (0-360) is obtained.
     *     </ul>
     *     <ul>
     * 3) The number of metres the user needs to travel is obtained for both the X-axis and Y-axis,
     * using a Pythagorean triangle. (This is an approximation as the world is not flat and leads to
     * inaccurate values for large distances, but is good enough for the purposes of getting lost).
     *     </ul>
     *     <ul>
     * 4) A LatLng is constructed using these values, is sanitised, then returned.
     *     </ul>
     * </li>
     *
     * @param approxDistance the approximate distance in kilometres that the user wishes to travel
     * @param initialPos the start location of the journey
     * @return a random destination that is roughly the approximate distance from the
     * current location (+/- 30%)
     */
    public static LatLng getRandomCoordinates(double approxDistance, LatLng initialPos) {
        // see http://stackoverflow.com/questions/2839533/adding-distance-to-a-gps-coordinate

        double radiansFactor = (180 / Math.PI);

        double journeyDistanceMetres = getRandomMetres(approxDistance);
        int directionAngle = getRandomJourneyDirection(); // hypotenuse length

        double xOffset = 0.0;
        double yOffset = 0.0;

        if (directionAngle == 0) {
            yOffset = journeyDistanceMetres;
        }
        else if (directionAngle == 90) {
            xOffset = journeyDistanceMetres;
        }
        else {
            yOffset = getYOffset(directionAngle, journeyDistanceMetres);
            xOffset = getXOffset(journeyDistanceMetres, yOffset);
        }

        double deltaLat = radiansFactor * (yOffset / EARTH_RADIUS);
        double deltaLng = ((radiansFactor) * (xOffset / EARTH_RADIUS)) / Math.cos(radiansFactor * initialPos.latitude);

        // decide whether to move up or down the map
        double randomLat = (new Random().nextBoolean()) ? initialPos.latitude + deltaLat : initialPos.latitude - deltaLat;
        double randomLng = (new Random().nextBoolean()) ? initialPos.longitude + deltaLng : initialPos.longitude - deltaLng;

        return getSanitisedPosition(randomLat, randomLng);
    }

    /**
     * Generates a random number of metres within the approxDistance (+/- 30%)
     *
     * @param approxDistance the overall distance the user will travel on a journey
     * @return a random number of metres that is within the desired range
     */
    public static double getRandomMetres(double approxDistance) {
        double delta = (approxDistance / 100) * DELTA_DISTANCE_FACTOR;

        double minAxis = (approxDistance - delta);
        double maxAxis = (approxDistance + delta);

        double range = maxAxis - minAxis;
        double scale = new Random().nextDouble() * range;
        return scale + minAxis;
    }

    public static int getRandomJourneyDirection() {
        return new Random().nextInt(90);
    }

    /**
     * This sanitises the newly generated LatLng in the event that the user lives at the GMT line
     * (e.g. London) or very far north (e.g. North Pole)
     *
     * latitude goes from 90 to -90, and then from -90 to 90
     * longitude goes from 0 to 180, then -180 to 0.
     *
     * @param randomLat new random latitude value
     * @param randomLng new random longitude value
     * @return a sanitised LatLng object that is guaranteed to be in the correct format for latlng
     */
    public static LatLng getSanitisedPosition(double randomLat, double randomLng) {

        if (randomLat < -90.0) {
            double absDiff = (randomLat + 90.0) * -1;
            randomLat = 90.0 - absDiff;
            randomLat *= -1;
        }
        else if (randomLat > 90.0) {
            double absDiff = randomLat - 90.0;
            randomLat = 90.0 - absDiff;
        }

        if (randomLng < -180.0) {
            double absDiff = (randomLng + 180.0) * -1;
            randomLng = 180.0 - absDiff;
        }
        else if (randomLng > 180.0) {
            double absDiff = randomLng - 180.0;
            randomLng = 180 - absDiff;
            randomLng *= -1;
        }

        return new LatLng(randomLat, randomLng);
    }

    private static double getYOffset(int directionAngle, double journeyDistanceMetres) {
        return Math.sin(directionAngle) * journeyDistanceMetres; // b = sin(c) c
    }

    private static double getXOffset(double journeyDistanceMetres, double yOffset) {
        return Math.sqrt(Math.pow(journeyDistanceMetres, 2) - Math.pow(yOffset, 2)); // a^2 = c^2 - b^2
    }

}
