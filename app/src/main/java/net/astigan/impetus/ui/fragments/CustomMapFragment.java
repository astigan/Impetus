package net.astigan.impetus.ui.fragments;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Date;
import java.util.List;

/**
 * A subclass of the standard google MapFragment, with added functions to update the UI
 * by adding markers etc.
 */
public class CustomMapFragment extends MapFragment {

    private static final float DEFAULT_MAP_ZOOM = 13.25f;
    private static final float STREET_LEVEL_ZOOM = 15.0f; // when journey starts & user is zoomed in

    private static final int DEFAULT_ANIM_TIME = 1000; // 1s
    private static final int SEEKBAR_CHANGE = 500; // 500ms
    private static final int POLYLINE_WIDTH = 2;

    private GoogleMap map;

    private Marker startMarker;
    private Marker finishMarker;
    private Marker currentMarker;

    private Polyline journeyLine;
    private Polyline guideLine;

    private long firstZoomUpdate = -1;

    public static CustomMapFragment newInstance() {
        return new CustomMapFragment();
    }

    public void setUpMapIfNeeded() {
        if (map == null) {
            map = getMap();
        }
    }

    public void startJourney(LatLng currentLatLng, LatLng destination) {
        resetMapMarkers();
        setStartLocation(currentLatLng);
        setFinishLocation(destination);
        setGuideLine(currentLatLng, destination);
        zoomToStreetLevel(currentLatLng);
    }

    public void endJourney(LatLng currentLatLng) {
        resetMapMarkers();
        zoomToUserLocation(currentLatLng);
    }

    public void setStartLocation(LatLng position) {
        if (startMarker != null) {
            startMarker.remove();
        }
        startMarker = map.addMarker(new MarkerOptions().position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    public void setFinishLocation(LatLng position) {
        if (finishMarker != null) {
            finishMarker.remove();
        }
        finishMarker = map.addMarker(new MarkerOptions().position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
    }

    public void approxDistanceChanged(LatLng currentLatLng, int approxDistance) {

        float currentZoom = map.getCameraPosition().zoom;
        float updatedZoom;

        if (approxDistance >= 0.0 && approxDistance < 2.5) {
            updatedZoom = 13.00f;
        }
        else if (approxDistance >= 2.5 && approxDistance < 5.0) {
            updatedZoom = 11.00f;
        }
        else if (approxDistance >= 5.0 && approxDistance < 10.0) {
            updatedZoom = 9.75f;
        }
        else if (approxDistance >= 10.0 && approxDistance < 17.0) {
            updatedZoom = 9.00f;
        }
        else if (approxDistance >= 17.0 && approxDistance < 25.0) {
            updatedZoom = 8.50f;
        }
        else if (approxDistance >= 25.0 && approxDistance < 40.0) {
            updatedZoom = 7.75f;
        }
        else if (approxDistance >= 40.0) {
            updatedZoom = 7.50f;
        }
        else {
            updatedZoom = DEFAULT_MAP_ZOOM;
        }

        if (finishMarker != null) {
            finishMarker.remove();
        }

        if (currentZoom != updatedZoom && currentLatLng != null) {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, updatedZoom);
            map.animateCamera(update, SEEKBAR_CHANGE, null);
        }
    }

    public void setCurrentLocation(LatLng position) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = map.addMarker(new MarkerOptions().position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        if (firstZoomUpdate == -1 || (new Date().getTime()) - firstZoomUpdate < 1000) {
            firstZoomUpdate = new Date().getTime();
            zoomToUserLocation(position);
        }
        else {
            centerOnUserLocation(position);
        }
    }

    public void setJourneyRoute(List<LatLng> positions) {
        if (journeyLine == null) {
            setupJourneyLine(positions);
        } else {
            journeyLine.setPoints(positions);
        }
    }

    public void resetMapMarkers() {
        if (startMarker != null) {
            startMarker.remove();
        }
        if (finishMarker != null) {
            finishMarker.remove();
        }
        if (journeyLine != null) {
            journeyLine.remove();
        }
        if (guideLine != null) {
            guideLine.remove();
        }
    }

    private void setupJourneyLine(List<LatLng> positions) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(POLYLINE_WIDTH);

        for (LatLng point : positions) {
            polylineOptions.add(point);
        }
        journeyLine = map.addPolyline(polylineOptions);
    }

    private void zoomToUserLocation(LatLng position) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(position, DEFAULT_MAP_ZOOM);
        map.animateCamera(update, DEFAULT_ANIM_TIME, null);
    }

    private void zoomToStreetLevel(LatLng currentLatLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, STREET_LEVEL_ZOOM);
        map.animateCamera(update, DEFAULT_ANIM_TIME, null);
    }

    private void centerOnUserLocation(LatLng currentLatLng) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, map.getCameraPosition().zoom);
        map.animateCamera(update, DEFAULT_ANIM_TIME, null);
    }

    private void setGuideLine(LatLng startLocation, LatLng finishLocation) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(POLYLINE_WIDTH);

        polylineOptions.add(startLocation);
        polylineOptions.add(finishLocation);

        guideLine = map.addPolyline(polylineOptions);
    }

}