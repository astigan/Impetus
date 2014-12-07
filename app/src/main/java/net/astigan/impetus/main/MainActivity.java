package net.astigan.impetus.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;

import net.astigan.impetus.R;
import net.astigan.impetus.entities.Journey;
import net.astigan.impetus.log.Logger;
import net.astigan.impetus.ui.fragments.CustomMapFragment;
import net.astigan.impetus.ui.fragments.JourneyCreatorFragment;
import net.astigan.impetus.ui.fragments.NavigatorFragment;
import net.astigan.impetus.utils.LocationUtils;
import net.astigan.impetus.utils.MapUtils;

/**
 * Manages fragment transactions, and app state.
 */
public class MainActivity extends BaseDrawerLayoutActivity implements JourneyCreatorFragment.JourneyCreatorFragmentListener, NavigatorFragment.NavigatorFragmentListener {

    private enum AppMode {
        CREATE_JOURNEY,
        NAVIGATE_JOURNEY
    }

    private static final int GOOGLE_PLAY_REQUEST_CODE = 0;
    private static final long VIBRATE_TIME = 500; // 500ms

    private AppMode appMode;

    private NavigatorFragment navigatorFragment;
    private JourneyCreatorFragment journeyCreator;
    private CustomMapFragment mapFragment;

    private Location destination;
    private LatLng currentLatLng;
    private Journey journey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        boolean playServicesAvailable = (statusCode == ConnectionResult.SUCCESS);

        if (!playServicesAvailable) {
            handlePlayServicesUnavailable(statusCode);
        } else {
            initialise();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mapFragment != null) {
            mapFragment.setUpMapIfNeeded();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerList)) {
            drawerLayout.closeDrawer(drawerList);
        } else if (appMode == AppMode.NAVIGATE_JOURNEY) {
            showConfirmDialog(R.string.end_journey_title, R.string.end_journey_msg, new FinishJourneyListener());
        } else {
            stopService(new Intent(this, LocationService.class));
            super.onBackPressed();
        }
    }

    private void initialise() {
        appMode = AppMode.CREATE_JOURNEY;

        if (journeyCreator == null) {
            journeyCreator = JourneyCreatorFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, journeyCreator).commit();
        }
        if (mapFragment == null) {
            mapFragment = CustomMapFragment.newInstance();
            getFragmentManager().beginTransaction().replace(R.id.map_fragment_container, mapFragment).commit();
        }
    }

    private void handlePlayServicesUnavailable(int statusCode) {
        Crashlytics.log(Log.INFO, Logger.TAG, "Google play services unavailable - " + statusCode);

        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, this, GOOGLE_PLAY_REQUEST_CODE);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }

    protected void onLocationUpdate(double lat, double lng) {
        updateMapLocationIfNeeded(lat, lng);
        updateIfJourneyActive();
    }

    private void updateMapLocationIfNeeded(double lat, double lng) {
        if (lat != -1 && lng != -1) {
            currentLatLng = new LatLng(lat, lng);
            if (mapFragment != null) {
                mapFragment.setCurrentLocation(currentLatLng);
            }
            Log.d(Logger.TAG, "Got location: " + currentLatLng);
        } else {
            Crashlytics.log("Null location was broadcast!");
        }
    }

    private void updateIfJourneyActive() {
        if (appMode == AppMode.NAVIGATE_JOURNEY) {

            if (journey == null) {
                journey = locationService.getJourney();
            }

            mapFragment.setJourneyRoute(journey.getLocationRoute());
        }
    }

    private void showConfirmDialog(int title, int message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(title));
        builder.setMessage(getString(message));
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, listener);
        builder.create().show();
    }

    @Override
    public void onRandomiseJourney(int distanceKm) {
        if (currentLatLng == null) {
            informUserNoLocAvailable();
            journeyCreator.informNoJourneyAvailable();
        } else {
            LatLng coords = MapUtils.getRandomCoordinates(distanceKm, currentLatLng);
            destination = LocationUtils.constructLocationFromLatLng(coords);
            mapFragment.setFinishLocation(coords);
        }
    }

    @Override
    public void onJourneyStart() {
        if (currentLatLng == null) {
            informUserNoLocAvailable();
        } else {
            try {
                locationService.startNewJourney(destination);

                appMode = AppMode.NAVIGATE_JOURNEY;
                navigatorFragment = NavigatorFragment.newInstance();

                getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, navigatorFragment).commit();

                mapFragment.startJourney(currentLatLng, LocationUtils.constructLatLngFromLocation(destination));

            } catch (RuntimeException e) {
                Crashlytics.logException(e);

                Toast.makeText(this, getString(R.string.generic_error), Toast.LENGTH_SHORT).show();
                appMode = AppMode.CREATE_JOURNEY;
            }
        }
    }

    @Override
    public void onJourneyEnd() {
        showConfirmDialog(R.string.end_journey_title, R.string.end_journey_msg, new FinishJourneyListener());
    }

    private void informUserNoLocAvailable() {
        Toast.makeText(this, getString(R.string.current_location_unavailable), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSeekbarChanged(int distanceKm) {
        mapFragment.approxDistanceChanged(currentLatLng, distanceKm);
    }

    private class FinishJourneyListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            endJourney();
        }
    }

    private void endJourney() {
        locationService.stopJourney();
        mapFragment.endJourney(currentLatLng);
        getFragmentManager().beginTransaction().replace(R.id.main_fragment_container, JourneyCreatorFragment.newInstance()).commit();
        appMode = AppMode.CREATE_JOURNEY;
    }

}