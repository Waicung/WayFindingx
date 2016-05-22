package com.waicung.wayfindingx;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.waicung.wayfindingx.handlers.DatabaseHandler;

/**
 * Created by waicung on 11/05/2016.
 */
public class GPStrackingService extends Service {
    private DatabaseHandler db;
    private long interval;   // 3 seconds, in milliseconds
    private float minDisplacement;
    private int currentStep;
    private final IBinder mBinder = new LocalBinder();
    LocationManager locationManager;
    Location lastLocation;
    double longitudeGPS, latitudeGPS;
    private final String TAG = "TracingService";

    public GPStrackingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        db = new DatabaseHandler(getApplicationContext());
        //when the service is created
        interval = 10 * 1000;   // 10 seconds, in milliseconds
        minDisplacement = 0;

    }

    @Override
    public IBinder onBind(Intent intent) {
        if (db.getData() != null) {
            db.newTable();
        }
        toggleGPSUpdates();
        Log.i(TAG, "starting connection");
        setStep(1);
        // Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        locationManager.removeUpdates(locationListenerGPS);
        return super.onUnbind(intent);
    }

    private boolean handleNewLocation(Location location, String event) {
        long currentTime = System.currentTimeMillis() / 1000;
        if (location != null) {
            Log.i(TAG, "get a location update for step: " + currentStep);
            double mCurrentLatitude = location.getLatitude();
            double mCurrentLongitude = location.getLongitude();
            db.insertLocation(mCurrentLatitude, mCurrentLongitude, currentTime, currentStep, event);
            return true;
        } else {
            if (event != null) {
                db.insertLocation((double) 0, (double) 0, currentTime, currentStep, event);
            }
            db.insertLocation((double) 0, (double) 0, currentTime, currentStep, "Lose location update");
            Toast.makeText(this, getString(R.string.no_location_detected), Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    public boolean setStep(int step) {
        Log.i(TAG, "save location for step: " + currentStep);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        boolean result = handleNewLocation(lastLocation, "");
        this.currentStep = step;
        return result;
    }

    public boolean setLog(int step, String event) {
        Log.i(TAG, "record event for step:  " + currentStep + " ," + event);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        boolean result = handleNewLocation(lastLocation, event);
        this.currentStep = step;
        return result;


    }

    //Location method
    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    public void toggleGPSUpdates() {
        if(!checkLocation())
            return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 0, locationListenerGPS);

    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            if(lastLocation==null){
                Toast.makeText(getApplicationContext(), "GPS is on", Toast.LENGTH_LONG).show();
            }
            lastLocation = location;
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();
            handleNewLocation(location,null);
                    Toast.makeText(getApplicationContext(), "GPS signal update" +
                            longitudeGPS + " " + latitudeGPS + "", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public GPStrackingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GPStrackingService.this;
        }
    }
}
