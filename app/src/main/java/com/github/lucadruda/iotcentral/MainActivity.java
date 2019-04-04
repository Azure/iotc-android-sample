package com.github.lucadruda.iotcentral;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lucadruda.iotcentral.adapters.AppAdapter;
import com.github.lucadruda.iotcentral.helpers.LoadingAlert;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.DataClient;
import com.github.lucadruda.iotcentral.service.exceptions.DataException;
import com.microsoft.aad.adal.AuthenticationContext;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    /* UI & Debugging Variables */
    private static final String TAG = MainActivity.class.getSimpleName();
    Button loginButton;
    Button signOutButton;

    private String userId;
    private AuthenticationContext authContext;

    private Application[] apps;
    private ArrayList<Button> appButtons;
    private ExpandableGrid gridView;
    private FloatingActionButton newAppBtn;
    private DataClient iotcDataClient;

    private LoadingAlert appLoadingAlert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authContext = Authentication.create(getActivity(), this);
        appLoadingAlert = new LoadingAlert(this, "Loading applications");

        loginButton = (Button) findViewById(R.id.login);
        signOutButton = (Button) findViewById(R.id.logout);
        appButtons = new ArrayList<Button>();

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onLoginClicked();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSignOutClicked();
            }
        });

        gridView = (ExpandableGrid) findViewById(R.id.gridApps);
        gridView.setNumColumns(3);
        newAppBtn = (FloatingActionButton) findViewById(R.id.newapp);

        newAppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createIntent = new Intent(getActivity(), ApplicationCreationActivity.class);
                // createIntent.putExtra("app", apps[position]);
                startActivityForResult(createIntent, 0);
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        ((TextView) v.findViewById(R.id.grid_item_label))
                                .getText(), Toast.LENGTH_SHORT).show();

            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        loginButton.callOnClick();
/*        if (BuildConfig.DEBUG) {
            final String iotctoken = getResources().getString(R.string.auth);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getAuthCallback().onSuccess(iotctoken, "Luca");
                }
            }).start();
        }*/
    }

    //
    // Core Auth methods used by ADAL
    // ==================================
    // onActivityResult() - handles redirect from System browser
    // onLoginClicked() - attempts to get tokens for iotcentral, if it succeeds calls iotcentral & updates UI
    // onSignOutClicked() - Signs user out of the app & updates UI
    //

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            updateApps(null, null);

        } else {
            Authentication.onActivityResult(authContext, requestCode, resultCode, data);
        }
    }

    /*
     * End user clicked login button, time for Auth
     * Use ADAL to get an Access token for IoTCentral
     */
    private void onLoginClicked() {
        Authentication.getToken(authContext, Constants.IOTC_TOKEN_AUDIENCE, getAuthCallback());
    }


    private void onSignOutClicked() {
        // End user has clicked the Sign Out button
        // Kill the token cache
        // Optionally call the signout endpoint to fully sign out the user account
        Authentication.cleanCache(authContext);
        updateSignedOutUI();
    }

    //
    // UI Helper methods
    // ================================
    // updateSuccessUI() - Updates UI when token acquisition succeeds
    // updateSignedOutUI() - Updates UI when app sign out succeeds
    //


    @SuppressLint("SetTextI18n")
    private void updateSuccessUI(String text, String userName) {
        // Called on success from /me endpoint
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        findViewById(R.id.welcome).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.welcome)).setText("Welcome, " +
                userName);

        gridView.setAdapter(new AppAdapter(this, apps));
        gridView.setExpanded(true);
        gridView.setVisibility(View.VISIBLE);
        findViewById(R.id.newAppContainer).setVisibility(View.VISIBLE);

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
        signOutButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);

    }

    @SuppressLint("SetTextI18n")
    private void updateSignedOutUI() {
        loginButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.GONE);
        gridView.setVisibility(View.GONE);
    }

    //
    // ADAL Callbacks
    // ======================
    // getActivity() - returns activity so we can acquireToken within a callback
    // getAuthSilentCallback() - callback defined to handle acquireTokenSilent() case
    // getAuthInteractiveCallback() - callback defined to handle acquireToken() case
    //

    public Activity getActivity() {
        return this;
    }


    private TokenCallback getAuthCallback() {
        return new TokenCallback() {
            @Override
            public void onSuccess(String token, final String userName) {
                updateApps(token, userName);
            }

            @Override
            public void onError(String errorMsg) {

            }
        };
    }

    private void updateApps(String token, final String userName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appLoadingAlert.start();
            }
        });
        try {
            if (token != null && token.length() > 0) {
                iotcDataClient = IoTCentral.createDataClient(token);
            }
            apps = iotcDataClient.listApps();

            final String text;
            if (userName != null && userName.length() > 0) {
                text = "Found " + apps.length + " applications";
            } else {
                text = "";
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    appLoadingAlert.stop();
                    updateSuccessUI(text, userName);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        }

    }

}
