package com.ma.monitoringlibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class Measurement {

    private Context context;
    private SharedPreferences preferences;
    private static final int reportingRate = 25;
    private static final int queuingRate = 10;

    private static double PercentageHttp = 0.8;
    private static double PercentageDNS = 0.8;

    //initializing tools
    static public void init(Activity activity) {
        new Configuration(activity);
    }


    public Measurement(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(context.getString(R.string.monitoringPref), MODE_PRIVATE);
        //check tool is initializing
        if (!appIsInitialized())
            throw new RuntimeException("APP is not initialized via Measurement library : Call Measurement#init first");
    }

    private boolean appIsInitialized() {
        return preferences.getBoolean(context.getString(R.string.AppIsInit), false);
    }

    protected static void setPercentageHttp(double percentageHttp) {
        if (percentageHttp > 0 && percentageHttp <= 1)
            PercentageHttp = percentageHttp;
        else
            throw new RuntimeException("double percentageHttp must be between 1 and 0 , 1 for 100% ");

    }

    protected static void setPercentageDNS(double percentDANS) {
        if (percentDANS > 0 && percentDANS <= 1)
            PercentageDNS = percentDANS;
        else
            throw new RuntimeException("double percentDANS must be between 1 and 0 , 1 for 100%  ");

    }

    public synchronized int start(Type type) {
        int id = 1;

        if (type == Type.HTTP) {
            id = preferences.getInt(context.getString(R.string.HttpIdCounter), 1);
            preferences.edit().putInt(context.getString(R.string.HttpIdCounter), id + 1).apply();
        } else if (type == Type.DNS) {
            id = preferences.getInt(context.getString(R.string.DnsIdCounter), 1);
            preferences.edit().putInt(context.getString(R.string.DnsIdCounter), id + 1).apply();
        }

        long currentTime = System.currentTimeMillis();
        preferences.edit().putLong("Time start :" + id + "_" + type, currentTime).apply();
        return id;
    }

    @SuppressLint("SimpleDateFormat")
    public synchronized void end(Type type, int id) {
        long currentTime = System.currentTimeMillis();

        long startTime = preferences.getLong("Time start :" + id + "_" + type, -1);
        if (startTime == -1)
            throw new RuntimeException("id not recorded");

        long measuredTime = currentTime - startTime;


        Date currentData = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

        SharedPreferences.Editor editor = preferences.edit();

        float longLoc = preferences.getFloat("location long", -1);
        float latLoc = preferences.getFloat("location lat", -1);

        editor.putFloat("Time total :" + id + "_" + type, measuredTime);
        editor.putFloat("Location long :" + id + "_" + type, longLoc);
        editor.putFloat("Location lat :" + id + "_" + type, latLoc);
        editor.putString("Data :" + id + "_" + type, sdf.format(currentData));
        editor.apply();

        Log.e("measurements array", String.valueOf(id));

        if (id % queuingRate == 0)
            createPackage(id, type);
    }


    private void createPackage(int currentId, Type type) {
        int numberOfRecordedMeasures = preferences.getInt("numberOfRecordedMeasures", 0);
        Log.e("measurements array", "numberOfRecordedMeasures" + String.valueOf(numberOfRecordedMeasures));

        if (numberOfRecordedMeasures == 0)
            newReportingPackage(currentId, type);
        else
            updateReportingPackage(currentId, type, numberOfRecordedMeasures);
    }

    private void newReportingPackage(int currentId, Type type) {
        int numberOfItems = (int) (queuingRate * (type == Type.HTTP ? PercentageHttp : PercentageDNS));

        int firstId = (currentId - queuingRate) + 1;
        JSONObject object = new JSONObject();


        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;

            String developerID = bundle.getString("DeveloperID");
            String appID = bundle.getString("AppID");
            String deviceID = preferences.getString(context.getString(R.string.deviceID), "not Yet");

            object.put("developerID", developerID);
            object.put("appID", appID);
            object.put("deviceID", deviceID);


            JSONArray jsonArray = new JSONArray();
            SharedPreferences.Editor editor = preferences.edit();
            for (int i = 0; i < numberOfItems; i++) {
                JSONObject measureObject = new JSONObject();

                long locationLong= (long) (preferences.getFloat("Location long :" + (firstId + i) + "_" + type, 200)*Math.pow(10.0, 15.0));
                long locationLat= (long) (preferences.getFloat("Location lat :" + (firstId + i) + "_" + type, 100)*Math.pow(10.0, 15.0));


                measureObject.put("Time total", preferences.getFloat("Time total :" + (firstId + i) + "_" + type, -1));
                measureObject.put("Location long",locationLong);
                measureObject.put("Location lat", locationLat);
                measureObject.put("date", preferences.getString("Data :" + (firstId + i) + "_" + type, ""));
                measureObject.put("type", type);

                jsonArray.put(measureObject);
                // clone preferences
                editor.remove("Time total :" + (firstId + i) + "_" + type);
                editor.remove("Location long :" + (firstId + i) + "_" + type);
                editor.remove("Location lat :" + (firstId + i) + "_" + type);
                editor.remove("Data :" + (firstId + i) + "_" + type);

            }

            editor.apply();

            object.put("measurements array", jsonArray);

            Log.e("JSONRecord", object.toString());
            //update number Of Recorded Measures
            preferences.edit().putInt("numberOfRecordedMeasures", numberOfItems).apply();
            preferences.edit().putString("measurementsJsonString", object.toString()).apply();


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateReportingPackage(int currentId, Type type, int numberOfRecordedMeasures) {

        String JsonString = preferences.getString("measurementsJsonString", null);
        if (JsonString == null) {
            preferences.edit().putInt("numberOfRecordedMeasures", 0).apply();
            return;
        }


        try {
            int numberOfItem = (int) (queuingRate * (type == Type.HTTP ? PercentageHttp : PercentageDNS));
            int firstId = (currentId - queuingRate) + 1;

            JSONObject packageObject = new JSONObject(JsonString);
            JSONArray jsonArray = packageObject.getJSONArray("measurements array");

            for (int i = 0; i < numberOfItem; i++) {
                JSONObject measureObject = new JSONObject();

                long locationLong= (long) (preferences.getFloat("Location long :" + (firstId + i) + "_" + type, 200)*Math.pow(10.0, 15.0));
                long locationLat= (long) (preferences.getFloat("Location lat :" + (firstId + i) + "_" + type, 100)*Math.pow(10.0, 15.0));


                measureObject.put("Time total", preferences.getFloat("Time total :" + (firstId + i) + "_" + type, -1));
                measureObject.put("Location long",locationLong);
                measureObject.put("Location lat", locationLat);
                measureObject.put("date", preferences.getString("Data :" + (firstId + i) + "_" + type, ""));
                measureObject.put("type", type);

                jsonArray.put(measureObject);
            }

            packageObject.put("measurements array", jsonArray);
            preferences.edit().putString("measurementsJsonString", packageObject.toString()).apply();

            int newNumberOfRecordedMeasures = numberOfRecordedMeasures + numberOfItem;
            if (newNumberOfRecordedMeasures >= reportingRate) {
                try {
                    Log.e("origin", String.valueOf(packageObject.toString()));

                    String compressedPackage = Compression.Compress(packageObject.toString());
                    String encryptPackage = new Encryption(String.valueOf(System.currentTimeMillis())).Encrypt(compressedPackage);

                  //  String encryptPackage = new Encryption(String.valueOf(System.currentTimeMillis())).Encrypt(packageObject.toString());
                  //  String compressedPackage = Compression.Compress(encryptPackage);

                    /*TODO Send encryptPackage */
                    Log.e("compression", String.valueOf(compressedPackage));
                    Log.e("encryption", String.valueOf(encryptPackage));


                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                preferences.edit().putInt("numberOfRecordedMeasures", newNumberOfRecordedMeasures).apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public enum Type {
        HTTP,
        DNS
    }
}
