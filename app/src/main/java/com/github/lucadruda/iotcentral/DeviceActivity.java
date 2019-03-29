package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.github.lucadruda.iotc.device.IoTCClient;
import com.github.lucadruda.iotc.device.enums.IOTC_CONNECT;
import com.github.lucadruda.iotc.device.exceptions.IoTCentralException;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.Device;
import com.github.lucadruda.iotcentral.service.DeviceCredentials;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DeviceActivity extends AppCompatActivity {

    private Application application;
    private String templateId;
    private RadioGroup deviceGroup;
    private RadioButton[] devicesRadio;
    private Button newBtn;
    private Button connectBtn;
    private ArrayList<Device> devices;
    private String newDeviceId;
    private DeviceCredentials credentials;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_activity);
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        templateId = (String) getIntent().getSerializableExtra(Constants.DEVICE_TEMPLATE_ID);
        getSupportActionBar().setTitle(application.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        deviceGroup = (RadioGroup) findViewById(R.id.devicesGroup);
        newBtn = (Button) findViewById(R.id.addDevice);
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Add new device");
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newDeviceId = input.getText().toString();
                        createThread.start();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();*/
                Intent appIntent = new Intent(getActivity(), DeviceScanActivity.class);
                appIntent.putExtra(Constants.APPLICATION, application);
                appIntent.putExtra(Constants.DEVICE_TEMPLATE_ID, templateId);
                startActivity(appIntent);
            }
        });
        connectBtn = (Button) findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceId = ((RadioButton) deviceGroup.findViewById(deviceGroup.getCheckedRadioButtonId())).getHint().toString();
                final IoTCClient
                        iotcclient = new IoTCClient(deviceId, credentials.getIdScope(), IOTC_CONNECT.SYMM_KEY, credentials.getPrimaryKey());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            iotcclient.Connect();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(
                                            getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IoTCentralException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        devThread.start();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

        }
        return true;
    }

    Thread devThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                credentials = IoTCentral.getDataClient().getCredentials(application.getId());

                devices = new ArrayList<Device>(Arrays.asList(IoTCentral.getDataClient().listDevices(application.getId(), templateId)));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDevices();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    Thread createThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Device newDevice = IoTCentral.getDataClient().createDevice(application.getId(), newDeviceId, templateId);
                devices.add(newDevice);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDevices();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    public Activity getActivity() {
        return this;
    }

    private void updateDevices() {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{

                        new int[]{-android.R.attr.state_enabled}, //disabled
                        new int[]{android.R.attr.state_enabled} //enabled
                },
                new int[]{

                        Color.WHITE //disabled
                        , Color.BLUE //enabled

                }
        );
        devicesRadio = new RadioButton[devices.size()];
        deviceGroup.removeAllViews();
        for (int i = 0; i < devices.size(); i++) {
            devicesRadio[i] = new RadioButton(getActivity());
            devicesRadio[i].setId(devicesRadio[i].hashCode());
            devicesRadio[i].setText(devices.get(i).getName());
            devicesRadio[i].setHint(devices.get(i).getDeviceId());
            devicesRadio[i].setTextColor(Color.WHITE);
            devicesRadio[i].setButtonTintList(colorStateList);//set the color tint list
            devicesRadio[i].invalidate(); //could not be necessary
            deviceGroup.addView(devicesRadio[i]);
        }
        deviceGroup.invalidate();
    }
}
