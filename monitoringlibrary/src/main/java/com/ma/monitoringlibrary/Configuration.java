package com.ma.monitoringlibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ma.monitoringlibrary.SettingActivity.SettingActivity;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class Configuration {

    private Activity activity;
    private SharedPreferences prefs;

    Configuration(Activity activity) {
        this.activity = activity;

        checkFirstLaunched();
    }


    @SuppressLint("MissingPermission")
    private void checkFirstLaunched() {

        prefs = activity.getApplicationContext().getSharedPreferences(activity.getString(R.string.monitoringPref), MODE_PRIVATE);

        if (!prefs.contains(activity.getString(R.string.AppIsInit))) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(activity.getString(R.string.AppIsInit), true);
            //editor.putInt(activity.getString(R.string.IdCounter),0);

            //first status set
            // true for enabled
            editor.putBoolean(activity.getString(R.string.ToolsStatus), true);
            editor.putBoolean(activity.getString(R.string.GpsStatus), true);

            editor.apply();
            // write location for first time
            writeLocation();
            // register device
            collectStatistics();

        } else {

            // write Location if location in enabled from user
            if (prefs.getBoolean(activity.getString(R.string.GpsStatus), false))
                writeLocation();


        }

    }

    @SuppressLint("MissingPermission")
    private void writeLocation() {

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putFloat("location lat", (float) location.getLatitude());
                    editor.putFloat("location long", (float) location.getLongitude());
                    editor.apply();

                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d("onStatusChanged", provider);
                }

                public void onProviderEnabled(String provider) {
                    Log.d("onProviderEnabled", provider);
                }

                public void onProviderDisabled(String provider) {
                    Log.d("onProviderDisabled", provider);
                    checkDeviceLocationIsOn();
                }
            };

            assert locationManager != null;
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        }

    }

    private boolean checkSelfPermission(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkDeviceLocationIsOn() {
        System.out.println("Test running setting request");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    // when exception catch the provider(GPS) is disable
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    // All location settings are satisfied.
                    Log.e("Location", "All location settings are satisfied. The client can initialize location");
                    writeLocation();

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                // i=100 for request code onActivityResult() (is redundant now)
                                resolvable.startResolutionForResult(activity, 100);
                            } catch (IntentSender.SendIntentException | ClassCastException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            Log.e("Location", "SETTINGS_CHANGE_UNAVAILABLE");
                            break;
                    }
                }
            }
        });
    }

    @SuppressLint("HardwareIds")
    private void collectStatistics() {

        int apiVersion = Build.VERSION.SDK_INT;
        String buildRelease = Build.VERSION.RELEASE;
        String osVersion = System.getProperty("os.version");
        String device = Build.DEVICE;
        String model = Build.MODEL;
        String product = Build.PRODUCT;

        TelephonyManager telephonyManager = ((TelephonyManager)
                activity.getSystemService(Context.TELEPHONY_SERVICE));
        assert telephonyManager != null;
        String operatorName = telephonyManager.getNetworkOperatorName();
        String SIMOperatorName = telephonyManager.getSimOperatorName();
        String serialNumber = android.provider.Settings.Secure.getString
                (activity.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        // save ANDROID_ID
        prefs.edit().putString(activity.getString(R.string.deviceID),serialNumber).apply();


        JSONObject jsonProperties = new JSONObject();
        try {
            jsonProperties.put("serialNumber", serialNumber);
            jsonProperties.put("apiVersion", apiVersion);
            jsonProperties.put("buildRelease", buildRelease);
            jsonProperties.put("osVersion", osVersion);
            jsonProperties.put("device", device);
            jsonProperties.put("model", model);
            jsonProperties.put("product", product);
            jsonProperties.put("operatorName", operatorName);
            jsonProperties.put("SIMOperatorName", SIMOperatorName);


            /*TODO Send jsonProperties as string  */

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonProperties.length() > 0) {
            //call to async class
            //String.valueOf(jsonProperties)
            //new SendJsonProperties().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jsonProperties); //instead of execute
           /* if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
                new SendJsonProperties(context).executeOnExecutor
                        (AsyncTask.THREAD_POOL_EXECUTOR, jsonProperties);
                Log.i("msg:1","calling asynctask" + jsonProperties.toString());
            } else {
                new SendJsonProperties(context).execute(jsonProperties);
                Log.i("msg","NOT calling asynctask");
            }
        }*/

            //new Thread( new StoreStatisticsTask( serialNumber, apiVersion, buildRelease, osVersion, device, model, product, operatorName, SIMOperatorName) ).start();
        }

    }

    public void showSettingActivity(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

}
