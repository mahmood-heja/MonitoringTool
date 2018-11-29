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
import com.ma.monitoringlibrary.Configuration;
import com.ma.monitoringlibrary.Measurement;
import com.ma.monitoringlibrary.RSACipher;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MainActivity extends AppCompatActivity {

    private Configuration configuration;
    private Measurement measurement;
    int idMeasurement = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Measurement.init(this);

        measurement = new Measurement(this);

        Bundle metadata =getMetaData(this);

        String t="{\"developerID\":\"abcXJ434\",\"appID\":\"abcXJ434\",\"deviceID\":\"0d038a0d1388edee\",\"measurements array\":[{\"Time total\":202,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":856,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":399,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":301,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":630,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":374,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":682,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":825,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":285,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":552,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":688,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":708,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":1050,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":813,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":939,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":1058,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":342,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":431,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":393,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":499,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":332,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":265,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":310,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":268,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":265,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":267,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":274,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":270,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":236,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":261,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":236,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\",\"type\":\"HTTP\"},{\"Time total\":226,\"Location long\":35838809967041016,\"Location lat\":32557422637939452,\"date\":\"2018\\/11\\/27\"," +
                "\"type\":\"HTTP\"}]}\n";

        try {
            t=Compression.Compress(t);

//            RSACipher rsa=new RSACipher();

  //          PublicKey publicKey=RSACipher.stringToPublicKey(rsa.getPublicKey("pkcs1-pem"));
            String encryptPackage=new RSACipher().encrypt(t);

            Log.e("ttttt",encryptPackage);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

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
                configuration.showSettingActivity(this);
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
