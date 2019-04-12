package com.t_doc41.thomaseyes;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName().toUpperCase();

    private BlockableScrollView scrollView;

    private UartService mService;
    private int mState = Constants.UART_PROFILE_DISCONNECTED;
    private BluetoothAdapter mBtAdapter;

    private AtomicBoolean writeable = new AtomicBoolean(true);

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
        {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize())
            {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname)
        {
            Log.w(MainActivity.class.getSimpleName().toUpperCase(), "Bluetooth service has disconnected. Component name:" + classname);
            mService = null;
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            final Intent mIntent = intent;

            if (action.equals(UartService.ACTION_GATT_CONNECTED))
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        mState = Constants.UART_PROFILE_CONNECTED;

                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final int MATCH_PARENT_ROW = TableRow.LayoutParams.MATCH_PARENT;
                                final int WRAP_CONTENT_ROW = TableRow.LayoutParams.WRAP_CONTENT;
                                final int MATCH_PARENT_TABLE = TableLayout.LayoutParams.MATCH_PARENT;
                                final int WRAP_CONTENT_TABLE = TableLayout.LayoutParams.WRAP_CONTENT;

                                TableLayout tl = (TableLayout) findViewById(R.id.table);
                                TableRow row;
                                if (tl.getChildCount() > 0)
                                {
                                    row = (TableRow) tl.getChildAt(tl.getChildCount() - 1);
                                }
                                else
                                {
                                    row = new TableRow(MainActivity.this);
                                    row.setLayoutParams(new TableRow.LayoutParams(MATCH_PARENT_ROW, WRAP_CONTENT_ROW));
                                    tl.addView(row);
                                }

                                TableRow rta2;
                                if (row.getChildCount() > 0 && row.getChildCount() % 2 != 0)
                                {
                                    rta2 = row;
                                }
                                else
                                {
                                    /* Create a new row to be added. */
                                    rta2 = new TableRow(MainActivity.this);
                                    rta2.setLayoutParams(new TableRow.LayoutParams(MATCH_PARENT_ROW, WRAP_CONTENT_ROW));
                                    tl.addView(rta2);
                                }

                                final ThomasJoyStick joystickView = new ThomasJoyStick(MainActivity.this)
                                {
                                    @Override
                                    public boolean onTouchEvent(MotionEvent event)
                                    {
                                        if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
                                        {
                                            scrollView.setScrollingEnabled(false);
                                        }
                                        else if (event.getActionMasked() == MotionEvent.ACTION_UP)
                                        {
                                            scrollView.setScrollingEnabled(true);
                                        }
                                        return super.onTouchEvent(event);
                                    }
                                };
                                TableRow.LayoutParams params = new TableRow.LayoutParams(MATCH_PARENT_ROW, 850, 1);

                                joystickView.setId(tl.getChildCount() + rta2.getChildCount());
                                joystickView.setLayoutParams(params);
                                joystickView.setBackgroundColor(Color.LTGRAY);
                                joystickView.setButtonColor(Color.YELLOW);
                                joystickView.setBorderWidth(8);
                                joystickView.setBorderColor(Color.GREEN);

                                joystickView.setOnMoveListener(new JoystickView.OnMoveListener()
                                {
                                    private int prevPulseX = -1;
                                    private int prevPulseY = -1;

                                    @Override
                                    public void onMove(int angle, int strength)
                                    {
                                        int currPulseX = joystickView.getNormalizedX();
                                        int currPulseY = joystickView.getNormalizedY();
                                        if (prevPulseX == -1 || currPulseX != prevPulseX)
                                        {
                                            mService.writeRXCharacteristic(String.valueOf("X" + currPulseX).getBytes());
                                            prevPulseX = currPulseX;
                                        }
                                        if (prevPulseY == -1 || currPulseY != prevPulseY)
                                        {
                                            mService.writeRXCharacteristic(String.valueOf("Y" + currPulseY).getBytes());
                                            prevPulseY = currPulseY;
                                        }
                                    }
                                }, 1);

                                rta2.addView(joystickView);
                            }//
                        });
                    }
                });
            }

            if (action.equals("write"))
            {
//                log("write");
            }

            if (action.equals(UartService.ACTION_GATT_DISCONNECTED))
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        mState = Constants.UART_PROFILE_DISCONNECTED;
                        mService.close();
                        toast("Disconnected");
                    }
                });
            }

            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED))
            {
                mService.enableTXNotification();
            }

            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART))
            {
                toast(getString(R.string.uart_not_supported));
                mService.disconnect();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = (BlockableScrollView) findViewById(R.id.scrollView);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null)
        {
            toast(getString(R.string.ble_not_supported));
            finish();
            return;
        }

        requestBluetoothPermissions();

        service_init();
    }

    private void service_init()
    {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction("write");

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, intentFilter);
    }

    private void requestBluetoothPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    public void openScanWindow(View view)
    {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        }
        else
        {
            //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
            Intent newIntent = new Intent(this, BLEScanActivity.class);
            startActivityForResult(newIntent, Constants.OPEN_SCAN_WINDOW);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                toast(getString(R.string.ble_enabled));
                //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                Intent newIntent = new Intent(this, BLEScanActivity.class);
                startActivityForResult(newIntent, Constants.OPEN_SCAN_WINDOW);
            }
        }
        else if (requestCode == Constants.OPEN_SCAN_WINDOW && data != null)
        {
            String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
            final BluetoothDevice mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
            toast(mDevice.getName());
            mService.connect(mDevice.getAddress());
            Button disconnect = (Button) findViewById(R.id.disconnect_button);
            disconnect.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Disconnect button pressed
                    if (mDevice != null)
                    {
                        mService.disconnect();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case Constants.PERMISSION_REQUEST_FINE_LOCATION:
            {
                if (grantResults != null && grantResults.length > 0)
                {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    {
                        toast(getString(R.string.ble_perms_denied));
                        finish();
                    }
                }
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!mBtAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try
        {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        }
        catch (Exception ignore)
        {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;
    }

    @Override
    public void onBackPressed()
    {
        if (mState == Constants.UART_PROFILE_CONNECTED)
        {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            toast(getString(R.string.running_in_background));
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }

    private void toast(final String msg)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void log(final String str)
    {
        Log.d(TAG, str);
    }
}

