package com.github.lucadruda.iotcentral;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lucadruda.iotcentral.adapters.AppAdapter;
import com.github.lucadruda.iotcentral.helpers.LoadingAlert;
import com.github.lucadruda.iotcentral.service.ARMClient;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.DataClient;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.github.lucadruda.iotcentral.service.types.Subscription;
import com.microsoft.aad.adal.AuthenticationContext;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends BaseActivity {


    private Application[] apps;
    private ArrayList<Button> appButtons;
    private ExpandableGrid gridView;
    private FloatingActionButton newAppBtn;
    private TextView welcomeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeView = findViewById(R.id.welcome);
        gridView = (ExpandableGrid) findViewById(R.id.gridApps);
        gridView.setNumColumns(3);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                TextView textView = (TextView) v
                        .findViewById(R.id.grid_item_label);
                textView.setText(apps[position].getName());
                Intent appIntent = new Intent(getActivity(), ApplicationActivity.class);
                appIntent.putExtra(Constants.APPLICATION, apps[position]);
                startActivity(appIntent);
                Toast.makeText(getApplicationContext(),
                        textView.getText(), Toast.LENGTH_SHORT).show();
            }
        });
        newAppBtn = (FloatingActionButton) findViewById(R.id.addBtn);

        newAppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createIntent = new Intent(getActivity(), ApplicationCreationActivity.class);
                // createIntent.putExtra("app", apps[position]);
                startActivityForResult(createIntent, 0);
            }
        });

        loadingAlert.start("Loading applications", false);
        try {
            enableRefresh(getRefreshListener());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            finish();
        }
        onLoginClicked();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            refresh();
        } else {
            Authentication.onActivityResult(authContext, requestCode, resultCode, data);
        }
    }


    @Override
    protected void onLoginSucceded(final String userName) {
        super.onLoginSucceded(userName);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                welcomeView.setText(getString(R.string.welcome_text) + userName);
                welcomeView.setVisibility(View.VISIBLE);
                refresh();
            }
        });

    }

    @Override
    protected void onLogoutClicked() {
        super.onLogoutClicked();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                welcomeView.setVisibility(View.GONE);
                gridView.setVisibility(View.INVISIBLE);
            }
        });
    }

    public Activity getActivity() {
        return this;
    }


    private SwipeRefreshLayout.OnRefreshListener getRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingAlert.start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        processApps();
                        MainActivity.this.onAppsRefreshed();
                    }
                }).start();

            }
        };
    }

    private void onAppsRefreshed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingAlert.stop();
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
                gridView.setAdapter(new AppAdapter(getActivity(), apps));
                gridView.setExpanded(true);
                gridView.setVisibility(View.VISIBLE);
                findViewById(R.id.floatingBox).setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), "Found " + apps.length + " application.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processApps() {
        try {
            apps = dataClient.listApps();
            // improve match to get template
            //Application[] armApps = armClient.listAllTenantApplications();
        } catch (DataException e) {
            e.printStackTrace();
        }
    }
}
