package com.yu.ma.monitoringtool;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ma.monitoringlibrary.Compression;
import com.ma.monitoringlibrary.Measurement;
import com.ma.monitoringlibrary.RSACipher;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MainActivity extends AppCompatActivity {

    private Measurement measurement;
    int idMeasurement = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Measurement.init(this);

        measurement = new Measurement(this);


    }

    public  Bundle getMetaData(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                Measurement.showSettingActivity(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void start_end(View view) {
        int id = view.getId();
        if (id == R.id.start)
            idMeasurement = measurement.start(Measurement.Type.HTTP);
        else
            measurement.end(Measurement.Type.HTTP, idMeasurement);

    }
}
