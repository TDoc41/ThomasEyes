package com.t_doc41.thomaseyes;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BLEScanActivity extends Activity
{
    public static final String LOG_TAG = BLEScanActivity.class.getSimpleName();
    private static final int SCAN_DURATION_MS = 10000;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private List<BluetoothDevice> deviceList;
    private DeviceAdapter deviceAdapter;
    private ServiceConnection onService;
    private Map<String, Integer> devRssiValues;
    private Handler handler;

    private boolean scanning;
    private TextView statusLabel;
    private Button cancelButton;
    private ListView newDevicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blescan);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.7), (int) (height * 0.7));

        handler = new Handler();
        statusLabel = (TextView) findViewById(R.id.statusLabel);
        cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isScanning())
                {
                    finish();
                }
                else
                {
                    scanLeDevice(true);
                }
            }
        });
        deviceList = new ArrayList<BluetoothDevice>();
        deviceAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();
        newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(deviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            toast(getString(R.string.ble_not_supported));
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null)
        {
            toast(getString(R.string.ble_not_supported));
            return;
        }

        scanner = bluetoothAdapter.getBluetoothLeScanner();

        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable)
    {
        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable)
        {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    scanning = false;
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    cancelButton.setText(R.string.scan);
                }
            }, SCAN_DURATION_MS);

            scanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
            cancelButton.setText(R.string.cancel);
        }
        else
        {
            scanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            cancelButton.setText(R.string.scan);
        }
    }

    private void addDevice(BluetoothDevice device, int rssi)
    {
        boolean deviceFound = false;
        for (BluetoothDevice listDev : deviceList)
        {
            if (listDev.getAddress().equals(device.getAddress()))
            {
                deviceFound = true;
                break;
            }
        }
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound)
        {
            deviceList.add(device);
            statusLabel.setVisibility(View.GONE);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        bluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    protected void onPause()
    {
        super.onPause();
        scanLeDevice(false);
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            BluetoothDevice device = deviceList.get(position);
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());
            Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback()
            {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            addDevice(device, rssi);
                        }
                    });
                }
            };

    private void toast(String msg)
    {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
    }

    public boolean isScanning()
    {
        return scanning;
    }

    public void setScanning(boolean scanning)
    {
        this.scanning = scanning;
    }

    private class DeviceAdapter extends BaseAdapter
    {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;

        public DeviceAdapter(Context context, List<BluetoothDevice> devices)
        {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount()
        {
            return devices.size();
        }

        @Override
        public Object getItem(int position)
        {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewGroup vg;
            if (convertView != null)
            {
                vg = (ViewGroup) convertView;
            }
            else
            {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }
            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);
            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0)
            {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }
            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            {
                Log.i(LOG_TAG, "device::" + device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            else
            {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
                tvrssi.setTextColor(Color.WHITE);
            }
            return vg;
        }
    }
}
