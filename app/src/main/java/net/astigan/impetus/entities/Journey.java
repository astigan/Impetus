package net.astigan.impetus.entities;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import net.astigan.impetus.utils.LocationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a journey. Locations along the journey are added to a list
 */
public class Journey {

    private List<Location> locations;

    private final Location startLocation;
    private final Location endLocation;
    private Location currentLocation;

    public Journey(Location startLocation, Location currentLocation, Location endLocation) {
        this.locations = new ArrayList<Location>();
        this.startLocation = startLocation;
        this.currentLocation = currentLocation;
        this.endLocation = endLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void addLocation(Location location) {
        this.locations.add(location);
    }

    public List<LatLng> getLocationRoute() {
        List<LatLng> route = new ArrayList<LatLng>();

        for (Location location : locations) {
            route.add(LocationUtils.constructLatLngFromLocation(location));
        }
        return route;
    }

    public int getJourneyDistance() {
        float distance = 0.0f;

        if (locations != null && locations.size() < 2) {

            for (int i=0; i < locations.size() - 2;) {
                Location a = locations.get(i);
                Location b = locations.get(++i);
                distance += a.distanceTo(b);
            }
        }

        return (int) distance;
    }
}
