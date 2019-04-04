package com.github.lucadruda.iotcentral;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.lucadruda.iotcentral.adapters.BLEAdapter;
import com.github.lucadruda.iotcentral.service.Application;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends AppCompatActivity {
    private BLEAdapter devicesAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothAdapter bluetoothAdapter;
    private RecyclerView scannedView;
    private boolean mScanning;
    private Handler mHandler;
    private boolean deviceExists;
    private String deviceName;
    private Application application;
    private String templateId;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan_activity);
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        templateId = getIntent().getStringExtra(Constants.DEVICE_TEMPLATE_ID);
        deviceExists = getIntent().getBooleanExtra(Constants.DEVICE_EXISTS, false);
        deviceName = getIntent().getStringExtra(Constants.DEVICE_NAME);
        getSupportActionBar().setTitle(application.getName() + " - " + getString(R.string.scanBleTitle));
        scannedView = findViewById(R.id.scannedView);
        scannedView.setHasFixedSize(true);
        scannedView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mHandler = new Handler();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

// Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bleScanner == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                devicesAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case android.R.id.home:
                finish();

        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();


        checkPermissions(this);
        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        devicesAdapter = new BLEAdapter(this, onDeviceClickListener);
        scannedView.setAdapter(devicesAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        devicesAdapter.clear();
    }

/*    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }*/

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bleScanner.stopScan(bleScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bleScanner.startScan(bleScanCallback);
        } else {
            mScanning = false;
            bleScanner.startScan(bleScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Device scan callback.
    private ScanCallback bleScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devicesAdapter.addItem(result.getDevice());
                            devicesAdapter.notifyDataSetChanged();
                        }
                    });
                }

            };

    private View.OnClickListener onDeviceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final BluetoothDevice device = (BluetoothDevice) v.getTag();
            if (device == null) {
                return;
            }
            final Intent intent = new Intent(getActivity(), BLEActivity.class);

            if (deviceExists && deviceName != null) {
                intent.putExtra(Constants.DEVICE_EXISTS, true);

            } else {
                deviceName = device.getName();
            }
            intent.putExtra(Constants.DEVICE_NAME, deviceName);
            intent.putExtra(Constants.DEVICE_ADDRESS, device.getAddress());
            intent.putExtra(Constants.APPLICATION, application);
            intent.putExtra(Constants.DEVICE_TEMPLATE_ID, templateId);

            if (mScanning) {
                bleScanner.stopScan(bleScanCallback);
                mScanning = false;
            }
            startActivity(intent);
        }
    };

    private void checkPermissions(Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    public Activity getActivity() {
        return this;
    }
}