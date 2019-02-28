package com.github.lucadruda.iotcentral;

public interface TokenCallback {

    public void onSuccess(String token, String userName);

    public void onError(String errorMsg);
}
