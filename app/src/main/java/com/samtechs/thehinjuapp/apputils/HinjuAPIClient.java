package com.samtechs.thehinjuapp.apputils;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.ByteArrayInputStream;

public class HinjuAPIClient {
    private static final String API_BASE_URL = "http://172.20.10.4/api/";
    private static final String API_BASE_URL2 = "http://app-api.atwebpages.com/hinju/";

    private final AsyncHttpClient asyncHttpClient;
    private RequestParams requestParams;
    private AsyncHttpResponseHandler httpResponseHandler;

    private int tries;

    public HinjuAPIClient() {
        Log.i("REACH", "1 - public HinjuAPIClient()");
        asyncHttpClient = new AsyncHttpClient(false, 80, 433);
        tries = 0;
    }

    public void post(Context context, String relativeURL, RequestParams requestParams, AsyncHttpResponseHandler httpResponseHandler) {
        Log.i("REACH", "2A - public void post()");

        this.requestParams = requestParams;
        this.httpResponseHandler = httpResponseHandler;
        this.tries++;

        Log.i("REACH", "2A.1 - " + requestParams.toString());
        asyncHttpClient.addHeader("HINJU", "HINJU");
        asyncHttpClient.setEnableRedirects(true);

        asyncHttpClient.setTimeout(60000);
        asyncHttpClient.post(context, API_BASE_URL + relativeURL, requestParams, httpResponseHandler);
    }

    public void get(Context context, String relativeURL, RequestParams requestParams, AsyncHttpResponseHandler httpResponseHandler) {
        Log.i("REACH", "2B - public void post()");

        this.requestParams = requestParams;
        this.httpResponseHandler = httpResponseHandler;
        this.tries++;

        Log.i("REACH", "2B.1 - " + requestParams.toString());
        asyncHttpClient.addHeader("HINJU", "HINJU");
        asyncHttpClient.setEnableRedirects(true);

        asyncHttpClient.setTimeout(60000);
        asyncHttpClient.get(context, API_BASE_URL2 + relativeURL, requestParams, httpResponseHandler);
    }

    public String getResponseString(byte[] response) {
        ByteArrayInputStream byteArrayInputStr
                = new ByteArrayInputStream(response);

        StringBuilder responseString = new StringBuilder();
        int b;
        while ((b = byteArrayInputStr.read()) != -1) {
            char ch = (char) b;
            responseString.append(ch);
        }
        return (responseString.toString());
    }

    public int getTries() {
        return tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }
}
