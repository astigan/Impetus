package net.astigan.impetus.main;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;

import net.astigan.impetus.R;
import net.astigan.impetus.log.Logger;
import net.astigan.impetus.ui.dialogs.AboutDialogFragment;
import net.astigan.impetus.ui.dialogs.LicenseDialogFragment;
import net.astigan.impetus.ui.dialogs.PrivacyDialogFragment;
import net.astigan.impetus.ui.views.DrawerList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.fabric.sdk.android.Fabric;

/**
 * Base activity with DrawerLayout, Butterknife, Crashlytics, GPS widget updater, and
 * bound LocationService. Can be easily extended for further activities.
 */
public abstract class BaseDrawerLayoutActivity extends Activity {

    private static final long UPDATE_INTERVAL = 5000; // 5s

    LocationService locationService;
    private ActionBarDrawerToggle drawerToggle;

    private ImageView gpsWidgetImage;
    private final Handler updateHandler = new Handler();

    @InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @InjectView(R.id.navdrawer) DrawerList drawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        Fabric.with(this, new Crashlytics());

        setupDrawerLayout();
        setupGpsWidget();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, impetusConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver,
                new IntentFilter(LocationService.LOCATION_UPDATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(impetusConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateHandler.removeCallbacks(gpsStatusUpdater);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(drawerList)) {
                drawerLayout.closeDrawer(drawerList);
            } else {
                drawerLayout.openDrawer(drawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerLayout() {
        DrawerArrowDrawable drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                drawerArrow, R.string.open,
                R.string.close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawerList.setOnItemClickListener(new DrawerLayoutClickAdapter());
    }

    private class DrawerLayoutClickAdapter implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            drawerLayout.closeDrawer(drawerList);

            switch (position) {
                case 0:
                    AboutDialogFragment aboutDialogFragment = AboutDialogFragment.newInstance();
                    aboutDialogFragment.show(getFragmentManager(), "");
                    break;
                case 1:
                    startRateApp();
                    break;
                case 2:
                    startShareApp();
                    break;
                case 3:
                    PrivacyDialogFragment privacyDialogFragment = PrivacyDialogFragment.newInstance();
                    privacyDialogFragment.show(getFragmentManager(), "");
                    break;
                case 4:
                    LicenseDialogFragment licenseDialogFragment = LicenseDialogFragment.newInstance();
                    licenseDialogFragment.show(getFragmentManager(), "");
                    break;
            }
        }

        private void startRateApp() {
            String appUrl = "https://play.google.com/store/apps/details?id=" + getPackageName();
            Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl));
            startActivity(rateIntent);
        }

        private void startShareApp() {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.open) + "\n" +
                    "Get Lost : https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(share, getString(R.string.app_name)));
        }
    }

    private void setupGpsWidget() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View gpsWidget = inflater.inflate(R.layout.gps_status, null);

        gpsWidgetImage = (ImageView) gpsWidget.findViewById(R.id.gps_widget_image);
        gpsWidget.setLayoutParams(new ActionBar.LayoutParams(Gravity.RIGHT));

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            ab.setCustomView(gpsWidget);
            ab.setDisplayShowCustomEnabled(true);
        }

        gpsStatusUpdater.run();
    }

    private final Runnable gpsStatusUpdater = new Runnable() {
        @Override
        public void run() {
            updateGpsWidgetStatus();
            updateHandler.postDelayed(gpsStatusUpdater, UPDATE_INTERVAL);
        }
    };

    private void updateGpsWidgetStatus() {
        if (locationService == null) {
            Crashlytics.log("Location service null when attempting to update gps widget");
        }
        else {
            if (!isGpsEnabled(this)) {
                gpsWidgetImage.setImageResource(R.drawable.ic_gps_off_white_36dp);
            }
            else {
                int resId = (locationService.isGpsFix()) ? R.drawable.ic_gps_fixed_white_36dp :
                        R.drawable.ic_gps_not_fixed_white_36dp;
                gpsWidgetImage.setImageResource(resId);
            }
        }
    }

    private final ServiceConnection impetusConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            LocationService.ImpetusBinder impetusBinder = (LocationService.ImpetusBinder) binder;
            locationService = impetusBinder.getService();
            Log.i(Logger.TAG, "Connected Impetus location service");
        }

        public void onServiceDisconnected(ComponentName className) {
            locationService = null;
            Log.i(Logger.TAG, "Disconnected Impetus location service");
        }
    };

    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double lat = intent.getDoubleExtra(LocationService.LAT_KEY, -1);
            double lng = intent.getDoubleExtra(LocationService.LNG_KEY, -1);

            onLocationUpdate(lat, lng);
        }
    };

    protected abstract void onLocationUpdate(double lat, double lng);

    private boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
