package com.t_doc41.thomaseyes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private static final int OPEN_SCAN_WINDOW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestBluetoothPermissions();
    }

    public void openScanWindow(View view)
    {
        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
        Intent newIntent = new Intent(MainActivity.this, BLEScanActivity.class);
        startActivityForResult(newIntent, OPEN_SCAN_WINDOW);
    }

    private void requestBluetoothPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, getString(R.string.ble_perms_granted), Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(this, getString(R.string.ble_perms_denied), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }
}

