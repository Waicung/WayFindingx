package com.waicung.wayfindingx;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.waicung.wayfindingx.handlers.DatabaseHandler;

/**
 * Created by waicung on 04/05/2016.
 * Tracing server for tracing the location of participants
 * based on Google play location service
 */
public class TracingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private DatabaseHandler db;
    private long interval = 3 * 1000;   // 3 seconds, in milliseconds
    private long fastestInterval = 1000;  // 1 second, in milliseconds
    private float minDisplacement;
    private int currentStep;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private int counter = 0; //counting the request failed
    private final String TAG = "TracingService";

    public TracingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DatabaseHandler(getApplicationContext());
        //when the service is created
        interval = 10 * 1000;   // 10 seconds, in milliseconds
        fastestInterval = 1000;  // 1 second, in milliseconds
        minDisplacement = 0;
        mGoogleApiClient = createGoogleApiClient();
        mLocationRequest = createLocationRequest();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        Log.i(TAG, "starting connection");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
        if (db.getData() != null) {
            db.newTable();
        }
        mGoogleApiClient.connect();
        Log.i(TAG, "starting connection");
        // Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
        Log.i(TAG, "Tracking Service is destroyed");
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        if(!locationAvailability.isLocationAvailable()){
                            counter+=1;
                            Log.i(TAG, " " + locationAvailability.isLocationAvailable());
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_location_detected) +" "+
                                    counter, Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startLocationUpdates();
                                }
                            }, 1000);
                        }
                    }
                }, null);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        setStep(1); // record the beginning and set step to 1
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location, "");
    }

    private void handleNewLocation(Location location, String event) {
        long currentTime = System.currentTimeMillis() / 1000;
        if (location != null) {
            Log.i(TAG, "get a location update for step: " + currentStep);
            double mCurrentLatitude = location.getLatitude();
            double mCurrentLongitude = location.getLongitude();
            db.insertLocation(mCurrentLatitude, mCurrentLongitude, currentTime, currentStep, event);
        } else {
            if (event != null) {
                db.insertLocation((double) 0, (double) 0, currentTime, currentStep, event);
            }
            db.insertLocation((double) 0, (double) 0, currentTime, currentStep, "Lose location update");
            Toast.makeText(this, getString(R.string.no_location_detected), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startLocationUpdates();
                }
            }, 1000);

        }

    }

    public void setStep(int step) {
        Log.i(TAG, "save location for step: " + currentStep);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        handleNewLocation(location, "");
        this.currentStep = step;
    }

    public void setLog(int step, String event) {
        Log.i(TAG, "record event for step:  " + currentStep + " ," + event);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        handleNewLocation(location, event);
        this.currentStep = step;

    }


    public GoogleApiClient createGoogleApiClient(){
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public LocationRequest createLocationRequest() {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(interval)
                .setFastestInterval(fastestInterval)
                .setSmallestDisplacement(minDisplacement);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult  .getErrorCode());
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public TracingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TracingService.this;
        }
    }
}
