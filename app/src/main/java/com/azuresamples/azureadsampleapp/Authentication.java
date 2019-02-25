package com.azuresamples.azureadsampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.github.lucadruda.iotcentral.service.Constants;
import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.IDispatcher;
import com.microsoft.aad.adal.ITokenCacheStore;
import com.microsoft.aad.adal.Logger;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.aad.adal.Telemetry;
import com.microsoft.aad.adal.TokenCacheItem;
import com.microsoft.aad.adal.UserInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Authentication {

    /* Boolean variable to ensure invocation of interactive sign-in only once in case of multiple  acquireTokenSilent call failures */
    private static AtomicBoolean sIntSignInInvoked = new AtomicBoolean();
    /* Constant to send message to the mAcquireTokenHandler to do acquire token with Prompt.Auto*/
    private static final int MSG_INTERACTIVE_SIGN_IN_PROMPT_AUTO = 1;
    /* Constant to send message to the mAcquireTokenHandler to do acquire token with Prompt.Always */
    private static final int MSG_INTERACTIVE_SIGN_IN_PROMPT_ALWAYS = 2;

    /* Constant to store user id in shared preferences */
    private static final String USER_ID = "user_id";
    private static final String TAG = "ADALAUTH";

    /* Azure AD Constants */
    /* Authority is in the form of https://login.microsoftonline.com/yourtenant.onmicrosoft.com */
    private static final String AUTHORITY = "https://login.microsoftonline.com/common";
    /* The clientID of your app_activity is a unique identifier which can be obtained from the app registration portal */
    private static final String CLIENT_ID = Constants.CLIENT_ID;
    /* Resource URI of the endpoint which will be accessed */
    /* The Redirect URI of the app_activity (Optional) */
    private static final String REDIRECT_URI = "http://localhost";

    /* Azure AD Variables */
    private static ITokenCacheStore mainCache;


    private static Context appContext;
    private static Activity appActivity;

    public static UserInfo getUserInfo() {
        return userInfo;
    }

    private static UserInfo userInfo;

    private static final boolean sTelemetryAggregationIsRequired = true;

    /* Telemetry dispatcher registration */
    static {
        Telemetry.getInstance().registerDispatcher(new IDispatcher() {
            @Override
            public void dispatchEvent(Map<String, String> events) {
                // Events from ADAL will be sent to this callback
                for (Map.Entry<String, String> entry : events.entrySet()) {
                    Log.d(TAG, entry.getKey() + ": " + entry.getValue());
                }
            }
        }, sTelemetryAggregationIsRequired);
        /* ADAL Logging callback setup */

        Logger.getInstance().setExternalLogger(new Logger.ILogger() {
            @Override
            public void Log(String tag, String message, String additionalMessage, Logger.LogLevel level, ADALError errorCode) {
                // You can filter the logs  depending on level or errorcode.
                Log.d(TAG, message + " " + additionalMessage);
            }
        });
    }

    public static AuthenticationContext create(final Activity activity, Context context) {
        AuthenticationContext authContext = create(activity, context, AUTHORITY);
        mainCache = authContext.getCache();
        return authContext;
    }

    public static AuthenticationContext create(final Activity activity, Context context, String authority) {
        appContext = context;
        appActivity = activity;
        if (mainCache != null) {
            return new AuthenticationContext(context, authority, true, mainCache);
        } else {
            return new AuthenticationContext(context, authority, true);
        }
    }
/*    public static void switchTenant(String authority) {
        Iterator<TokenCacheItem> cache = mAuthContext.getCache().getAll();
        while (cache.hasNext()) {
            TokenCacheItem item = cache.next();
            Log.d(TAG, "Authority: " + item.getAuthority());
            Log.d(TAG, "Tenant: " + item.getTenantId());
            Log.d(TAG, "Token: " + item.getAccessToken());
            Log.d(TAG, "RToken: " + item.getRefreshToken());
            Log.d(TAG, "Resource: " + item.getResource());
        }
        mAuthContext = new AuthenticationContext(appContext, authority, true);
    }*/


    public static void getToken(AuthenticationContext authContext, String
            resource, TokenCallback callback) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String userId = preferences.getString(USER_ID, "");
        if (!TextUtils.isEmpty(userId)) {
            authContext.acquireTokenSilentAsync(resource, CLIENT_ID, userId, getAuthSilentCallback(authContext, resource, callback));
        } else {
            getTokenWithPrompt(authContext, resource, PromptBehavior.Auto, callback);
        }
    }

    public static void cleanCache(AuthenticationContext authContext) {
        authContext.getCache().removeAll();
    }


    private static void getTokenWithPrompt(AuthenticationContext authContext, String
            resource, PromptBehavior behavior, TokenCallback callback) {
        if (behavior.equals(PromptBehavior.Auto)) {
            authContext.acquireToken(appActivity, resource, CLIENT_ID, REDIRECT_URI, PromptBehavior.Auto, getAuthInteractiveCallback(authContext, resource, callback));
        } else {
            authContext.acquireToken(appActivity, resource, CLIENT_ID, REDIRECT_URI, PromptBehavior.Always, getAuthInteractiveCallback(authContext, resource, callback));
        }
    }

    public static void onActivityResult(AuthenticationContext authContext, int requestCode,
                                        int resultCode, Intent data) {
        authContext.onActivityResult(requestCode, resultCode, data);
    }

    /* Instantiate handler which can invoke interactive sign-in to get the Resource
     * sIntSignInInvoked ensures interactive sign-in is invoked one at a time */;

    /* *//* ADAL Logging callback setup *//*

        Logger.getInstance().

    setExternalLogger(new Logger.ILogger() {
        @Override
        public void Log (String tag, String message, String additionalMessage, Logger.LogLevel
        level, ADALError
        errorCode){
            // You can filter the logs  depending on level or errorcode.
            android.util.Log.d(TAG, message + " " + additionalMessage);
        }
    });*/


    /* Callback used for interactive request.  If succeeds we use the access
     * token to call IoTCentral. Does not check cache
     */
    private static AuthenticationCallback<AuthenticationResult> getAuthInteractiveCallback(
            final AuthenticationContext authContext, final String resource, final TokenCallback callback) {
        return new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                if (authenticationResult == null || TextUtils.isEmpty(authenticationResult.getAccessToken())
                        || authenticationResult.getStatus() != AuthenticationResult.AuthenticationStatus.Succeeded) {
                    callback.onError("Authentication Result is invalid");
                    return;
                }
                Log.d(TAG, "Authority: " + authenticationResult.getAuthority());
                Log.d(TAG, "Resource: " + authenticationResult.getIsMultiResourceRefreshToken());
                Log.d(TAG, "Token: " + authenticationResult.getAccessToken());
                Log.d(TAG, "RToken: " + authenticationResult.getRefreshToken());
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
                preferences.edit().putString(USER_ID, authenticationResult.getUserInfo().getUserId()).apply();
                sIntSignInInvoked.set(false);
                userInfo = authenticationResult.getUserInfo();
                callback.onSuccess(authenticationResult.getAccessToken(), authenticationResult.getUserInfo().getGivenName());
            }

            @Override
            public void onError(Exception exception) {
                if (exception == null) {
                    return;
                }

                /* Failed to acquireToken */
                String msg = "Authentication failed: " + exception.toString();
                if (exception instanceof AuthenticationException) {
                    ADALError error = ((AuthenticationException) exception).getCode();
                    if (error == ADALError.AUTH_FAILED_CANCELLED) {
                        msg = "The user cancelled the authorization request";
                    } else if (error == ADALError.AUTH_FAILED_NO_TOKEN) {
                        // In this case ADAL has found a token in cache but failed to retrieve it.
                        // Retry interactive with Prompt.Always to ensure we do an interactive sign in
                        getTokenWithPrompt(authContext, resource, PromptBehavior.Auto, callback);
                        return;
                    } else if (error == ADALError.NO_NETWORK_CONNECTION_POWER_OPTIMIZATION) {
                        /* Device is in Doze mode or App is in stand by mode.
                           Wake up the app or show an appropriate prompt for the user to take action
                           More information on this : https://github.com/AzureAD/azure-activedirectory-library-for-android/wiki/Handle-Doze-and-App-Standby */
                        msg = "Device is in doze mode or the app is in standby mode";
                    }
                }
                /* set the sIntSignInInvoked boolean back to false  */
                sIntSignInInvoked.set(false);
                callback.onError(msg);
            }
        };
    }

    private static AuthenticationCallback<AuthenticationResult> getAuthSilentCallback(
            final AuthenticationContext authContext, final String resource, final TokenCallback callback) {
        return new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
                if (authenticationResult == null || TextUtils.isEmpty(authenticationResult.getAccessToken())
                        || authenticationResult.getStatus() != AuthenticationResult.AuthenticationStatus.Succeeded) {
                    /* retry with interactive */
                    getTokenWithPrompt(authContext, resource, PromptBehavior.Auto, callback);
                    return;
                }
                /* Successfully got a token, call iotcentral now */
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appActivity);
                preferences.edit().putString(USER_ID, authenticationResult.getUserInfo().getUserId()).apply();
                Log.d(TAG, "Authority: " + authenticationResult.getAuthority());
                Log.d(TAG, "Resource: " + authenticationResult.getIsMultiResourceRefreshToken());
                Log.d(TAG, "Token: " + authenticationResult.getAccessToken());
                Log.d(TAG, "RToken: " + authenticationResult.getRefreshToken());
                userInfo = authenticationResult.getUserInfo();
                callback.onSuccess(authenticationResult.getAccessToken(), authenticationResult.getUserInfo().getGivenName());
            }

            @Override
            public void onError(Exception exception) {
                /* Failed to acquireToken */
                String msg = "Authentication failed: " + exception.toString();
                if (exception instanceof AuthenticationException) {
                    AuthenticationException authException = ((AuthenticationException) exception);
                    ADALError error = authException.getCode();
                    logHttpErrors(authException);
                    msg = authException.getMessage();
                    /*  Tokens expired or no session, retry with interactive */
                    if (error == ADALError.AUTH_REFRESH_FAILED_PROMPT_NOT_ALLOWED) {
                        getTokenWithPrompt(authContext, resource, PromptBehavior.Auto, callback);
                        return;
                    } else if (error == ADALError.NO_NETWORK_CONNECTION_POWER_OPTIMIZATION) {
                        /* Device is in Doze mode or App is in stand by mode.
                           Wake up the app or show an appropriate prompt for the user to take action
                           More information on this : https://github.com/AzureAD/azure-activedirectory-library-for-android/wiki/Handle-Doze-and-App-Standby */
                        callback.onError("Device is in doze mode or the app is in standby mode");
                        return;
                    }

                }
                /* Attempt an interactive on any other exception */
                getTokenWithPrompt(authContext, resource, PromptBehavior.Auto, callback);
            }
        };
    }

    private static void logHttpErrors(AuthenticationException authException) {
        int httpResponseCode = authException.getServiceStatusCode();
        Log.d(TAG, "HTTP Response code: " + authException.getServiceStatusCode());
        if (httpResponseCode < 200 || httpResponseCode > 300) {
            // logging http response headers in case of a http error.
            HashMap<String, List<String>> headers = authException.getHttpResponseHeaders();
            if (headers != null) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    sb.append(entry.getKey());
                    sb.append(":");
                    sb.append(entry.getValue().toString());
                    sb.append("; ");
                }
                Log.e(TAG, "HTTP Response headers: " + sb.toString());
            }
        }
    }

}
