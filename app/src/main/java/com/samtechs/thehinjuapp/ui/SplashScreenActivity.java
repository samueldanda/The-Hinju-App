package com.samtechs.thehinjuapp.ui;

import static com.google.firebase.encoders.json.BuildConfig.APPLICATION_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.installations.FirebaseInstallations;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.samtechs.thehinjuapp.R;
import com.samtechs.thehinjuapp.apputils.HinjuAPIClient;
import com.samtechs.thehinjuapp.apputils.InternetConnectivity;
import com.samtechs.thehinjuapp.ui.main.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SplashScreenActivity extends AppCompatActivity {
    private SharedPreferences session;

    private CircularProgressIndicator progressIndicator;

    private static final int SPLASH_TIMER = 5000;

    private String userID;
    private String buildModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setNavigationBarColor(getColor(R.color.md_theme_light_onPrimaryContainer));
        getWindow().setStatusBarColor(getColor(R.color.md_theme_light_onPrimaryContainer));

        setContentView(R.layout.activity_splash_screen);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> {
                final ObjectAnimator slideUp = ObjectAnimator.ofFloat(
                        splashScreenView,
                        View.TRANSLATION_Y,
                        0f,
                        -splashScreenView.getHeight()
                );
                slideUp.setInterpolator(new AnticipateInterpolator());
                slideUp.setDuration(200L);

                // Call SplashScreenView.remove at the end of your custom animation.
                slideUp.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        splashScreenView.remove();
                    }
                });

                // Run your animation.
                slideUp.start();
            });
        }

        session = getSharedPreferences(APPLICATION_ID + R.string.session_preference, Context.MODE_PRIVATE);

        progressIndicator = findViewById(R.id.circular_progress_indicator);

        new CountDownTimer(SPLASH_TIMER, 1000) {

            @Override
            public void onFinish() {

                FirebaseInstallations.getInstance().getId()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("Installations", "Installation ID: " + task.getResult());

                                InternetConnectivity internetConnectivity = new InternetConnectivity(SplashScreenActivity.this);
                                if (!internetConnectivity.isConnected()) {

                                    Snackbar.make(progressIndicator, R.string.internet_connection_unavailable_text, Snackbar.LENGTH_LONG)
                                            .setBackgroundTint(getResources().getColor(R.color.md_theme_light_error))
                                            .addCallback(new Snackbar.Callback(){
                                                @Override
                                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                                    super.onDismissed(transientBottomBar, event);
                                                    finish();
                                                }
                                            })
                                            .show();

                                    return;
                                }

                                userID = task.getResult();
                                buildModel = Build.MODEL;

                                Log.i("DATA", "userID: " + userID);
                                Log.i("DATA", "buildModel: " + buildModel);

                                checkUserAPICall();

                                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));

                                finish();
                            } else {
                                Log.e("Installations", "Unable to get Installation ID");
                            }
                        });

            }

            @Override
            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }

    private void checkUserAPICall() {
        HinjuAPIClient hinjuAPIClient = new HinjuAPIClient();
        AsyncHttpResponseHandler httpResponseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i("REACH", "3 - void onStart()");
//                showLoading(getWindow(), sendRequestMaterialButton, progressIndicator, true);
            }

            @Override
            public void onRetry(int retryNo) {
                super.onRetry(retryNo);
                System.out.println("Retry no: " + retryNo);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("REACH", "4 - public void onSuccess()");
                String responseJSONString = hinjuAPIClient.getResponseString(responseBody);

                Log.i("OUTPUT", String.valueOf(statusCode));
                Log.i("OUTPUT", responseJSONString);

                postCheckUserAPICall(statusCode, responseJSONString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("REACH", "4 - public void onFailure()");
                String responseJSONString = null;
                if (responseBody != null)
                    responseJSONString = hinjuAPIClient.getResponseString(responseBody);

                Log.i("OUTPUT", String.valueOf(statusCode));
                Log.i("OUTPUT", responseJSONString);

                postCheckUserAPICall(statusCode, responseJSONString);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                getOptionsAPICall();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }
        };

        RequestParams requestParams = new RequestParams();
        requestParams.put("user_id", userID);
        requestParams.put("device", buildModel);

        Log.i("requestParams", "user_id available - " + requestParams.has("user_id"));
        Log.i("requestParams", "user_id available - " + requestParams.has("device"));

        hinjuAPIClient.get(SplashScreenActivity.this, "user/check", requestParams, httpResponseHandler);
    }

    private void postCheckUserAPICall(int statusCode, String responseJSONString) {
        JSONObject responseJSONObject;
        JSONObject hinjuJSONObject;
        JSONObject checkUserResponseJSONObject;

        try {
            responseJSONObject = new JSONObject(responseJSONString);
            hinjuJSONObject = responseJSONObject.getJSONObject("hinju");
            checkUserResponseJSONObject = hinjuJSONObject.getJSONObject("check_user_response");

            if (statusCode == 200) {
                SharedPreferences.Editor editor = session.edit();
                editor.putString(getString(R.string.sp_user_id), checkUserResponseJSONObject.getString("id"));
                editor.putInt(getString(R.string.sp_wallet), checkUserResponseJSONObject.getInt("wallet"));
                editor.putString(getString(R.string.sp_date_created), checkUserResponseJSONObject.getString("date_created"));
                editor.putBoolean(getString(R.string.sp_new_user), checkUserResponseJSONObject.getBoolean("new_user"));
                editor.apply();

                Log.i("PREFERENCE SAVED", "postCheckUserAPICall shared preference saved successfully");

            } else if (statusCode == 400) {
                System.out.println(checkUserResponseJSONObject.getString("code"));
                System.out.println(checkUserResponseJSONObject.getString("message"));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void getOptionsAPICall() {
        HinjuAPIClient hinjuAPIClient = new HinjuAPIClient();
        AsyncHttpResponseHandler httpResponseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i("REACH", "3 - void onStart()");
            }

            @Override
            public void onRetry(int retryNo) {
                super.onRetry(retryNo);
                System.out.println("Retry no: " + retryNo);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("REACH", "4 - public void onSuccess()");
                String responseJSONString = hinjuAPIClient.getResponseString(responseBody);

                Log.i("OUTPUT", String.valueOf(statusCode));
                Log.i("OUTPUT", responseJSONString);

                postGetOptionsAPICall(statusCode, responseJSONString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("REACH", "4 - public void onFailure()");
                String responseJSONString = null;
                if (responseBody != null)
                    responseJSONString = hinjuAPIClient.getResponseString(responseBody);

                Log.i("OUTPUT", String.valueOf(statusCode));
                Log.i("OUTPUT", responseJSONString);

                postGetOptionsAPICall(statusCode, responseJSONString);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                userTokensAPICall();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }
        };
        RequestParams requestParams = new RequestParams();

        hinjuAPIClient.get(SplashScreenActivity.this, "options/list", requestParams, httpResponseHandler);
    }

    private void postGetOptionsAPICall(int statusCode, String responseJSONString) {
        JSONObject responseJSONObject;
        JSONObject hinjuJSONObject;
        JSONObject listOptionsResponseJSONObject;
        JSONArray optionsJSONArray;

        try {
            responseJSONObject = new JSONObject(responseJSONString);
            hinjuJSONObject = responseJSONObject.getJSONObject("hinju");
            listOptionsResponseJSONObject = hinjuJSONObject.getJSONObject("list_options_response");
            optionsJSONArray = listOptionsResponseJSONObject.getJSONArray("options");

            if (statusCode == 200) {
                SharedPreferences.Editor editor = session.edit();
                editor.putString(getString(R.string.sp_min_fuel_quantity), optionsJSONArray.getJSONObject(0).getString("value"));
                editor.putString(getString(R.string.sp_max_fuel_quantity), optionsJSONArray.getJSONObject(1).getString("value"));
                editor.putString(getString(R.string.sp_tax_per_litre), optionsJSONArray.getJSONObject(2).getString("value"));
                editor.putString(getString(R.string.sp_service_charge_per_litre), optionsJSONArray.getJSONObject(3).getString("value"));
                editor.putString(getString(R.string.sp_default_wallet_balance), optionsJSONArray.getJSONObject(4).getString("value"));
                editor.putString(getString(R.string.sp_diesel), String.valueOf(optionsJSONArray.getJSONObject(5).getInt("value")));
                editor.putString(getString(R.string.sp_petrol), String.valueOf(optionsJSONArray.getJSONObject(6).getInt("value")));

                editor.apply();

                Log.i("PREFERENCE SAVED", "postGetOptionsAPICall shared preference saved successfully");

            } else if (statusCode == 400) {
                System.out.println(listOptionsResponseJSONObject.getString("code"));
                System.out.println(listOptionsResponseJSONObject.getString("message"));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void userTokensAPICall() {
        HinjuAPIClient hinjuAPIClient = new HinjuAPIClient();
        AsyncHttpResponseHandler httpResponseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i("REACH", "3 - void onStart()");
            }

            @Override
            public void onRetry(int retryNo) {
                super.onRetry(retryNo);
                System.out.println("Retry no: " + retryNo);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("REACH", "4 - public void onSuccess()");
                String responseJSONString = hinjuAPIClient.getResponseString(responseBody);

                Log.i("OUTPUT", String.valueOf(statusCode));
                Log.i("OUTPUT", responseJSONString);

                postUserTokensAPICall(statusCode, responseJSONString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("REACH", "4 - public void onFailure()");
                String responseJSONString = null;
                if (responseBody != null)
                    responseJSONString = hinjuAPIClient.getResponseString(responseBody);

                Log.i("OUTPUT", String.valueOf(statusCode));
                Log.i("OUTPUT", responseJSONString);

                postUserTokensAPICall(statusCode, responseJSONString);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }
        };
        RequestParams requestParams = new RequestParams();
        requestParams.put("user_id", userID);

        hinjuAPIClient.get(SplashScreenActivity.this, "user/tokens", requestParams, httpResponseHandler);
    }

    private void postUserTokensAPICall(int statusCode, String responseJSONString) {
        if (statusCode == 200) {
            SharedPreferences.Editor editor = session.edit();
            editor.putString(getString(R.string.sp_user_tokens_json_string), responseJSONString);
            editor.apply();

            Log.i("PREFERENCE SAVED", "postUserTokensAPICall shared preference saved successfully");

        } else if (statusCode == 400) {
            JSONObject responseJSONObject;
            JSONObject hinjuJSONObject;
            JSONObject userTokensResponseJSONObject;

            try {
                responseJSONObject = new JSONObject(responseJSONString);
                hinjuJSONObject = responseJSONObject.getJSONObject("hinju");
                userTokensResponseJSONObject = hinjuJSONObject.getJSONObject("user_tokens_response");

                System.out.println(userTokensResponseJSONObject.getString("code"));
                System.out.println(userTokensResponseJSONObject.getString("message"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

}