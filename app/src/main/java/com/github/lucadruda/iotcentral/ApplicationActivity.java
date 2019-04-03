package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.github.lucadruda.iotcentral.adapters.IoTCAdapter;
import com.github.lucadruda.iotcentral.helpers.LoadingAlert;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;

import java.io.IOException;

public class ApplicationActivity extends AppCompatActivity {

    private Application application;
    private DeviceTemplate[] models;
    private RecyclerView scannedView;
    private LoadingAlert templateLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan_activity);
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        getSupportActionBar().setTitle(application.getName());
        scannedView = findViewById(R.id.scannedView);
        scannedView.setHasFixedSize(true);
        scannedView.setLayoutManager(new LinearLayoutManager(getActivity()));
        templateLoader = new LoadingAlert(this, "Loading models");
        templateLoader.start();
        iotcThread.start();

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

        }
        return true;
    }

    Thread iotcThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                models = IoTCentral.getDataClient().listTemplates(application.getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        IoTCAdapter dataAdapter = new IoTCAdapter(getActivity(), models, onDeviceClickListener);
                        scannedView.setAdapter(dataAdapter);
                        templateLoader.stop();
                    }
                });
            } catch (DataException e) {
                e.printStackTrace();
            }
        }
    });

    private View.OnClickListener onDeviceClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final DeviceTemplate template = (DeviceTemplate) v.getTag();
            if (template == null) {
                return;
            }
            Intent appIntent = new Intent(getActivity(), DeviceActivity.class);
            appIntent.putExtra(Constants.APPLICATION, application);
            appIntent.putExtra(Constants.DEVICE_TEMPLATE_ID, template.getId());
            startActivity(appIntent);
        }
    };

    public Activity getActivity() {
        return this;
    }
}
