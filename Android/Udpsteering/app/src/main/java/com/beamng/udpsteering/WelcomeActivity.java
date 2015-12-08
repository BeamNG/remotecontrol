package com.beamng.udpsteering;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onScanClick(View view) {
        Intent intent = new Intent(this, QRCodeScanner.class);
        startActivity(intent);
    }
}
