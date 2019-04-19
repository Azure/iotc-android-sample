package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.lucadruda.iotcentral.adapters.IoTCAdapter;
import com.github.lucadruda.iotcentral.helpers.LoadingAlert;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.github.lucadruda.iotcentral.service.types.DeviceTemplate;

public class ApplicationActivity extends BaseActivity {

    private Application application;
    private DeviceTemplate[] models;
    private RecyclerView templatesView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        application = (Application) getIntent().getSerializableExtra(Constants.APPLICATION);
        setTitle(application.getName());
        ((TextView) findViewById(R.id.listTitle)).setText("Models");
        templatesView = findViewById(R.id.listView);
        templatesView.setHasFixedSize(true);
        templatesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadingAlert.start("Loading models");
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
                models = dataClient.listTemplates(application.getId());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        IoTCAdapter dataAdapter = new IoTCAdapter(getActivity(), models, onDeviceClickListener);
                        templatesView.setAdapter(dataAdapter);
                        loadingAlert.stop();
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
