package com.ma.monitoringlibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
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
        int id = 0;

        if (type == Type.HTTP) {
            id = preferences.getInt(context.getString(R.string.HttpIdCounter), 0);
            preferences.edit().putInt(context.getString(R.string.HttpIdCounter), id + 1).apply();
        } else if (type == Type.DNS) {
            id = preferences.getInt(context.getString(R.string.DnsIdCounter), 0);
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        SharedPreferences.Editor editor = preferences.edit();

        float longLoc = preferences.getFloat("location long", -1);
        float latLoc = preferences.getFloat("location lat", -1);

        editor.putFloat("Time total :" + id + "_" + type, measuredTime);
        editor.putFloat("Location long :" + id + "_" + type, longLoc);
        editor.putFloat("Location lat :" + id + "_" + type, latLoc);
        editor.putString("Data :" + id + "_" + type, sdf.format(currentData));
        editor.apply();

        if (id % reportingRate == 0)
            createPackage(id, type);
    }


    private void createPackage(int currentId, Type type) {
        int numberOfItem = (int) (reportingRate * (type == Type.HTTP ? PercentageHttp : PercentageDNS));

        int firstId = (currentId - reportingRate) + 1;
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

            JSONArray jsonArray=new JSONArray();
            for(int i=0;i<reportingRate;i++){
                JSONObject measureObject=new JSONObject();

                measureObject.put("Time total",preferences.getFloat("Time total :" + (firstId+i) + "_" + type,-1));
                measureObject.put("Location long",preferences.getFloat("Location long :" + (firstId+i) + "_" + type,-1));
                measureObject.put("Location lat",preferences.getFloat("Location lat :" + (firstId+i) + "_" + type,-1));
                measureObject.put("date",preferences.getString("Data :" + (firstId+i) + "_" + type,""));

                jsonArray.put(measureObject);
            }

            object.put("Package",jsonArray);

            try {

                String compressedPacakge=Compression.Compress(object.toString());
                String encryptPackage=new Security(String.valueOf(System.currentTimeMillis())).Encrypt(compressedPacakge);



            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public enum Type {
        HTTP,
        DNS
    }
}
