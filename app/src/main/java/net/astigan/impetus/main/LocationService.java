package net.astigan.impetus.main;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.astigan.impetus.entities.Journey;
import net.astigan.impetus.log.Logger;

/**
 * Receives updates on the current location, and if a journey is active, adds this information to
 * it. Also receives updates for GPS status and determines whether the user has a GPS Fix or not.
 */
public class LocationService extends Service {

    public static final String LOCATION_UPDATE = "net.astigan.impetus.location.LOCATION_UPDATE";
    public static final String LNG_KEY = "LONGITUDE";
    public static final String LAT_KEY = "LATITUDE";

    private static final float MIN_DIST_INTERVAL = 20.0f; // 30m
    private static final long MIN_TIME_INTERVAL = 5 * 1000; // 5s
    private static final long GPS_FIX_TIME = 15 * 1000; // 15s

    private final IBinder binder = new ImpetusBinder();
    private boolean journeyActive = false;

    private Journey journey;
    private Location currentLocation;

    private boolean isGpsFix = false;
    private long lastLocationTimeMs = 0;

    public class ImpetusBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Logger.TAG, "Starting Impetus location service");
        initialiseLocationUpdates();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(Logger.TAG, "Binding Impetus Location Service");
        broadcastLocation(currentLocation);
        initialiseLocationUpdates();
        return binder;
    }

    private void initialiseLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_INTERVAL,
                MIN_DIST_INTERVAL, new LocationChangeListener());

        locationManager.addGpsStatusListener(new GpsFixListener());

        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        broadcastLocation(currentLocation);
    }

    public void startNewJourney(Location destination) {
        if (currentLocation != null) {
            journeyActive = true;
            journey = new Journey(currentLocation, currentLocation, destination);
        }
        else {
            throw new RuntimeException("Attempted to start journey without a location!");
        }
    }

    public void stopJourney() {
        journeyActive = false;
        journey = null;
    }

    public Journey getJourney() {
        return journey;
    }

    private void broadcastLocation(Location location) {
        if (location != null) {
            Intent intent = new Intent(LOCATION_UPDATE);
            intent.putExtra(LAT_KEY, location.getLatitude());
            intent.putExtra(LNG_KEY, location.getLongitude());

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.i(Logger.TAG, "Broadcasting new location");
        }
        else {
            Log.w(Logger.TAG, "No location available for broadcast");
        }
    }

    public boolean isGpsFix() {
        return isGpsFix;
    }

    @Override
    public void onDestroy() {
        Log.i(Logger.TAG, "Destroying Impetus location service");
        super.onDestroy();
    }

    private class GpsFixListener implements GpsStatus.Listener {
        public void onGpsStatusChanged(int event) {

            switch (event) {
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                    Log.i(Logger.TAG, "GPS event");
                    long now = SystemClock.elapsedRealtime();
                    isGpsFix = (now - lastLocationTimeMs) < GPS_FIX_TIME;
                    lastLocationTimeMs = now;
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:

                    Log.i(Logger.TAG, "GPS fix");
                    isGpsFix = true;
                    break;
            }
        }
    }

    private class LocationChangeListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            if (location != null) {
                currentLocation = location;
                broadcastLocation(currentLocation);
            }

            if (journeyActive && journey != null) {
                journey.addLocation(location);
                broadcastLocation(location);
            }
            lastLocationTimeMs = SystemClock.elapsedRealtime();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

}