package net.astigan.impetus.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Utilities class to convert between LatLng to Location and vice versa
 */
public class LocationUtils {

    public static final String LOCATION_PROVIDER = "IMPETUS";

    public static Location constructLocationFromLatLng(LatLng position) {
        Location coordinates = new Location(LOCATION_PROVIDER);
        coordinates.setLatitude(position.latitude);
        coordinates.setLongitude(position.longitude);
        return coordinates;
    }

    public static LatLng constructLatLngFromLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

}
