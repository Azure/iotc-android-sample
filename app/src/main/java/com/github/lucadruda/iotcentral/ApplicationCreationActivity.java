package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lucadruda.iotcentral.service.ARMClient;
import com.github.lucadruda.iotcentral.service.Application;
import com.github.lucadruda.iotcentral.service.DataClient;
import com.github.lucadruda.iotcentral.service.User;
import com.github.lucadruda.iotcentral.service.templates.DevKitTemplate;
import com.github.lucadruda.iotcentral.service.types.ResourceGroup;
import com.github.lucadruda.iotcentral.service.types.Subscription;
import com.github.lucadruda.iotcentral.service.types.Tenant;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class ApplicationCreationActivity extends AppCompatActivity {

    private Spinner tenantSpinner;
    private Spinner subscriptionsSpinner;
    private Spinner resourceGroupsSpinner;
    private Spinner regionSpinner;
    private TextView appNameText;
    private TextView urlText;
    private RadioGroup appType;
    private RadioButton trialBtn;
    private RadioButton paidBtn;

    private Tenant[] tenants;
    private Subscription[] subscriptions;
    private ResourceGroup[] resourceGroups;
    private String resourceGroup;
    private String region;

    private Button createBtn;

    private AlertDialog loadingAlert;
    private String refreshToken;
    private boolean authenticated = false;
    private AuthenticationContext authContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_create);
        tenantSpinner = findViewById(R.id.tenantSpinner);
        subscriptionsSpinner = findViewById(R.id.subscriptionsSpinner);
        resourceGroupsSpinner = findViewById(R.id.resourceGroupsSpinner);
        appNameText = findViewById(R.id.appNameText);
        urlText = findViewById(R.id.urlText);
        appType = findViewById(R.id.appTypeGroup);
        createBtn = findViewById(R.id.createAppBtn);
        regionSpinner = findViewById(R.id.regionSpinner);
        regionSpinner.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, getResources().getStringArray(R.array.ARMRegions)) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });
        trialBtn = findViewById(R.id.freeBtn);
        paidBtn = findViewById(R.id.paidBtn);


        appType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioBtnID = group.getCheckedRadioButtonId();

                RadioButton radioBtn = group.findViewById(radioBtnID);
                if (radioBtn.getId() == R.id.paidBtn) {
                    if (!authenticated) {
                        startLoader(getActivity());
                        authContext = Authentication.create(getActivity(), getApplicationContext());
                        Authentication.getToken(authContext, Constants.RM_TOKEN_AUDIENCE, getArmCallback());
                    }
                    findViewById(R.id.linkText).setVisibility(View.GONE);
                    findViewById(R.id.paidScroll).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.linkText).setVisibility(View.VISIBLE);
                    findViewById(R.id.paidScroll).setVisibility(View.GONE);
                }
            }
        });

        tenantSpinner.setPrompt("Select tenant");

        tenantSpinner.setOnItemSelectedListener(getTenenatSelection());
        subscriptionsSpinner.setOnItemSelectedListener(getSubscriptionSelection());


        findViewById(R.id.addRg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LinearLayout layout = new LinearLayout(getApplicationContext());
                builder.setTitle("Add new resource group");
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                layout.setOrientation(LinearLayout.VERTICAL);

                final Spinner spinner = new Spinner(getApplicationContext());
                spinner.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item, getResources().getStringArray(R.array.ARMRegions)));
                layout.addView(input);
                layout.addView(spinner);
                layout.setPadding(50, 40, 50, 10);
                builder.setView(layout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resourceGroup = input.getText().toString();
                        region = String.valueOf(spinner.getSelectedItem());
                        try {
                            IoTCentral.getArmClient().createResourceGroup(resourceGroup, Region.findByLabelOrName(region));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), resourceGroup + " successfully created", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (region == null || region.length() == 0) {
                        region = String.valueOf(regionSpinner.getSelectedItem());
                    }
                    if (resourceGroup == null || resourceGroup.length() == 0) {
                        resourceGroup = String.valueOf(resourceGroupsSpinner.getSelectedItem());
                    }
                    ARMClient iotc = IoTCentral.getArmClient();
                    Application application = new Application(appNameText.getText().toString(), appNameText.getText().toString(), urlText.getText().toString(), Region.findByLabelOrName(region).name(), new DevKitTemplate());
                    startLoader(getActivity());
                    iotc.setResourceGroup(resourceGroup);
                    iotc.createApplication(application);
                    stopLoader();
                    finish();
                    setResult(10);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Application can't be created. Check errors on the Azure portal for more details", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    private TokenCallback getSubscriptionsCallback() {
        return new TokenCallback() {
            @Override
            public void onSuccess(String token, final String userName) {
                try {
                    ARMClient armClient = IoTCentral.createARMClient(token);
                    try {
                        subscriptions = armClient.listSubscriptions();
                        if (subscriptions.length == 0) {
                            return;
                        }
                        final String[] subs = new String[subscriptions.length];
                        for (int i = 0; i < subscriptions.length; i++) {
                            subs[i] = subscriptions[i].getDisplayName();
                        }
                        // set first subscription as default and populate resourcegroups
                        armClient.setSubscription(subscriptions[0].getSubscriptionId());
                        updateRGs(subscriptions[0].getSubscriptionId());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> subscriptionsAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, subs) {
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        TextView textView = (TextView) super.getView(position, convertView, parent);
                                        textView.setTextColor(Color.WHITE);
                                        return textView;
                                    }
                                };
                                subscriptionsSpinner.setAdapter(subscriptionsAdapter);
                                stopLoader();
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(String errorMsg) {
                Log.d("AUTHERROR", errorMsg);
            }
        };
    }

    private TokenCallback getArmCallback() {
        return new TokenCallback() {
            @Override
            public void onSuccess(String token, final String userName) {
                try {
                    authenticated = true;
                    ARMClient armClient = IoTCentral.createARMClient(token);
                    try {
                        tenants = armClient.listTenants();
                        final String[] names = new String[tenants.length];
                        for (int i = 0; i < tenants.length; i++) {
                            names[i] = tenants[i].getDisplayName();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> tenantAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, names) {
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        TextView textView = (TextView) super.getView(position, convertView, parent);
                                        textView.setTextColor(Color.WHITE);
                                        return textView;
                                    }

                                };
                                stopLoader();
                                tenantSpinner.setAdapter(tenantAdapter);

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(String errorMsg) {
                Log.d("AUTHERROR", errorMsg);
            }
        };
    }

    public AdapterView.OnItemSelectedListener getTenenatSelection() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!authenticated) {
                    return;
                }
                startLoader(getActivity());
                authContext = Authentication.create(getActivity(), getApplicationContext(), Constants.AUTHORITY_BASE + tenants[position].getTenantId());
                Authentication.getToken(authContext, Constants.RM_TOKEN_AUDIENCE, getSubscriptionsCallback());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

    public AdapterView.OnItemSelectedListener getSubscriptionSelection() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String subscriptionId = subscriptions[position].getSubscriptionId();
                IoTCentral.getArmClient().setSubscription(subscriptionId);
                try {
                    updateRGs(subscriptionId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    private void updateRGs(final String subscriptionId) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resourceGroups = IoTCentral.getArmClient().listResourceGroups(subscriptionId);

                    final String[] rgs = new String[resourceGroups.length];
                    for (int i = 0; i < resourceGroups.length; i++) {
                        rgs[i] = resourceGroups[i].getName();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> rgAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, rgs) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    TextView textView = (TextView) super.getView(position, convertView, parent);
                                    textView.setTextColor(Color.WHITE);
                                    return textView;
                                }
                            };
                            resourceGroupsSpinner.setAdapter(rgAdapter);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public Activity getActivity() {
        return this;
    }

    public void startLoader(final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setView(R.layout.loading);
                builder.setCancelable(false);
                loadingAlert = builder.show();
            }
        });

    }

    public void stopLoader() {
        loadingAlert.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Authentication.onActivityResult(authContext, requestCode, resultCode, data);
    }

}
