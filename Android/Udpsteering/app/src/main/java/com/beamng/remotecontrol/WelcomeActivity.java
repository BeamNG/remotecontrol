package com.beamng.remotecontrol;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.beamng.remotecontrol.R;

public class WelcomeActivity extends Activity {

    public static int CamPermission;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onScanClick(View view) {

        if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i("BeamNG", "No Camera Permission");

                ActivityCompat.requestPermissions(WelcomeActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    CamPermission);


            return;
        }
        Intent intent = new Intent(this, QRCodeScanner.class);
        startActivity(intent);
    }
}
