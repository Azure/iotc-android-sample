package com.github.lucadruda.iotcentral;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.github.lucadruda.iotcentral.helpers.LoadingAlert;
import com.github.lucadruda.iotcentral.helpers.NotificationAlert;
import com.github.lucadruda.iotcentral.service.ARMClient;
import com.github.lucadruda.iotcentral.service.DataClient;
import com.microsoft.aad.adal.AuthenticationContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

public class BaseActivity extends AppCompatActivity {

    protected LoadingAlert loadingAlert;
    protected NotificationAlert notificationAlert;
    protected AuthenticationContext authContext;
    protected DataClient dataClient;
    protected ARMClient armClient;
    protected SwipeRefreshLayout refreshLayout;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;

    private Menu menu;

    public Activity getActivity() {
        return this;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View rootLayout = ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        if (rootLayout instanceof SwipeRefreshLayout) {
            refreshLayout = (SwipeRefreshLayout) rootLayout;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingAlert = new LoadingAlert(this);
        notificationAlert = new NotificationAlert(this);
        authContext = Authentication.create(this);
        dataClient = IoTCentral.getDataClient();
        armClient = IoTCentral.getArmClient();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void enableRefresh(SwipeRefreshLayout.OnRefreshListener listener) throws IllegalAccessException {
        if (refreshLayout != null) {
            refreshListener = listener;
            refreshLayout.setOnRefreshListener(refreshListener);
        } else {
            throw new IllegalAccessException("No RefreshLayout present in current activity");
        }
    }

    public void refresh() {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
                // directly call onRefresh() method
                refreshListener.onRefresh();
            }
        });
    }

    public void stopRefresh() {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
                // directly call onRefresh() method
            }
        });
    }

    public void disableRefresh() {
        refreshLayout.setEnabled(false);
        refreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.app, menu);
        super.onCreateOptionsMenu(menu);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userId = preferences.getString(Constants.USER_ID, "");
        String userName = preferences.getString(Constants.USER_NAME, "");
        String userEmail = preferences.getString(Constants.USER_EMAIL, "");
        menu.findItem(R.id.menu_logout).setOnMenuItemClickListener(getLogoutHandler());
        menu.findItem(R.id.menu_login).setOnMenuItemClickListener(getLoginHandler());
        if (TextUtils.isEmpty(userId)) {
            menu.setGroupVisible(R.id.menu_unlogged_group, true);
            menu.setGroupVisible(R.id.menu_logged_group, false);

        } else {
            menu.setGroupVisible(R.id.menu_unlogged_group, false);
            menu.setGroupVisible(R.id.menu_logged_group, true);
            menu.findItem(R.id.menu_user_name).setTitle(userName);
        }
        return true;
    }

    private TokenCallback getIoTCAuthCallback() {
        return new TokenCallback() {
            @Override
            public void onSuccess(String token, final String userName) {
                try {
                    if (token != null && token.length() > 0) {
                        dataClient = IoTCentral.createDataClient(token);
                        Authentication.getToken(authContext, Constants.RM_TOKEN_AUDIENCE, getARMCAuthCallback());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(String errorMsg) {

            }
        };
    }

    private TokenCallback getARMCAuthCallback() {
        return new TokenCallback() {
            @Override
            public void onSuccess(String token, final String userName) {
                try {
                    armClient = IoTCentral.createARMClient(token);
                    ((BaseActivity) getActivity()).onLoginSucceded(userName);
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

            }
        };
    }

    protected void onLoginSucceded(String userName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
                menu.setGroupVisible(R.id.menu_unlogged_group, false);
                menu.setGroupVisible(R.id.menu_logged_group, true);
            }
        });
    }

    protected void onLoginClicked() {
        Authentication.getToken(authContext, Constants.IOTC_TOKEN_AUDIENCE, getIoTCAuthCallback());
    }

    private MenuItem.OnMenuItemClickListener getLogoutHandler() {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onLogoutClicked();
                return true;
            }
        };
    }

    private MenuItem.OnMenuItemClickListener getLoginHandler() {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onLoginClicked();
                return true;
            }
        };
    }

    protected void onLogoutClicked() {
        Authentication.cleanCache(authContext);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                menu.setGroupVisible(R.id.menu_unlogged_group, true);
                menu.setGroupVisible(R.id.menu_logged_group, false);
            }
        });
    }

    protected void setTitle(String text) {
        getSupportActionBar().setTitle(text);
    }
}
