package com.samtechs.thehinjuapp.ui.payment.checkout;

import static com.google.firebase.encoders.json.BuildConfig.APPLICATION_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.samtechs.thehinjuapp.R;
import com.samtechs.thehinjuapp.apputils.HinjuAPIClient;
import com.samtechs.thehinjuapp.apputils.InternetConnectivity;
import com.samtechs.thehinjuapp.ui.main.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class WalletCheckOutActivity extends AppCompatActivity {
    private SharedPreferences session;

    private LinearProgressIndicator progressIndicator;

    private TextView balanceTextView;

    private Button copyTokenIconButton;
    private Button shareTokenIconButton;
    private TextView cardTokenTextView;
    private TextView cardFuelTextView;
    private TextView cardCostTextView;
    private TextView cardDateCreatedTextView;
    private MaterialCardView materialCardView;

    private MaterialButton processPaymentMaterialButton;

    private int selectedFuelType;
    private int fuelQuantity;
    private int totalCost;

    private static final int PAYMENT_TYPE = 100;

    private DecimalFormat decimalFormat;

    private boolean transactionComplete = false;

    private String userID;
    private String generatedToken;

    private long lastBytesWritten;
    private long lastTotalSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_wallet_check_out);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        selectedFuelType = intent.getIntExtra("FUEL_TYPE_EXTRA", 0);
        fuelQuantity = intent.getIntExtra("FUEL_QUANTITY", 0);
        totalCost = (int) intent.getLongExtra("TOTAL_COST", 0);

        session = getSharedPreferences(APPLICATION_ID + R.string.session_preference, Context.MODE_PRIVATE);
        userID = session.getString(getString(R.string.sp_user_id), "0");
        int wallet = session.getInt(getString(R.string.sp_wallet), 0);

        decimalFormat = new DecimalFormat("#,###.00");

        progressIndicator = findViewById(R.id.linear_progress_indicator);
        balanceTextView = findViewById(R.id.balance_text);
        cardTokenTextView = findViewById(R.id.cardTokenTextView);
        cardFuelTextView = findViewById(R.id.cardFuelTextView);
        cardCostTextView = findViewById(R.id.cardCostTextView);
        cardDateCreatedTextView = findViewById(R.id.cardDateCreatedTextView);
        copyTokenIconButton = findViewById(R.id.copyTokenIconButton);
        shareTokenIconButton = findViewById(R.id.shareTokenIconButton);
        materialCardView = findViewById(R.id.tokenCard);
        processPaymentMaterialButton = findViewById(R.id.process_payment_button);

        progressIndicator.setVisibility(View.INVISIBLE);
        balanceTextView.setText(getString(R.string.wallet_balance_text, decimalFormat.format(wallet)));

        materialCardView.setVisibility(View.INVISIBLE);
        processPaymentMaterialButton.setText(getString(R.string.process_payment_txt1, decimalFormat.format(totalCost)));

        copyTokenIconButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("token", generatedToken);
            clipboard.setPrimaryClip(clip);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Toast.makeText(WalletCheckOutActivity.this, "Copied", Toast.LENGTH_LONG).show();
        });

        shareTokenIconButton.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");

            String shareBody = generatedToken;

            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        });

        processPaymentMaterialButton.setOnClickListener(v -> {
            if (transactionComplete) {
                onBackPressed();
                return;
            }

            InternetConnectivity internetConnectivity = new InternetConnectivity(WalletCheckOutActivity.this);
            if (!internetConnectivity.isConnected()) {

                Snackbar.make(processPaymentMaterialButton, R.string.internet_connection_unavailable_text, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.md_theme_light_error))
                        .show();

                return;
            }

            processPaymentAPICall();
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!transactionComplete) {
            super.onBackPressed();
        } else {
            gotToTokensList();
        }
    }

    void gotToTokensList() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    private long dateStringToLong(String date) {
        Date newDate;
        long millis = 0;
        final long millisToAdd = 10_800_000; //3 hours
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
            newDate = simpleDateFormat.parse(date);
            if (newDate != null) {
                newDate.setTime(newDate.getTime() + millisToAdd);
            }
            if (newDate != null) {
                millis = newDate.getTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return millis;
    }

    private String relativeTimeString(long time) {
        return (String) DateUtils.getRelativeDateTimeString(WalletCheckOutActivity.this, time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_TIME);
    }

    private void processPaymentAPICall() {
        HinjuAPIClient hinjuAPIClient = new HinjuAPIClient();
        AsyncHttpResponseHandler httpResponseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i("REACH", "3 - void onStart()");
                progressIndicator.setVisibility(View.VISIBLE);
                processPaymentMaterialButton.setCheckable(false);
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

                postProcessPaymentAPICall(statusCode, responseJSONString);
                processPaymentMaterialButton.setText(getString(R.string.process_payment_txt2));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("REACH", "4 - public void onFailure()");
                String responseJSONString = null;
                if (responseBody != null)
                    responseJSONString = hinjuAPIClient.getResponseString(responseBody);

                Log.i("OUTPUT", String.valueOf(statusCode));
                Log.i("OUTPUT", responseJSONString);

                postProcessPaymentAPICall(statusCode, responseJSONString);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                progressIndicator.setMin(0);
                progressIndicator.setMax((int) totalSize);
                progressIndicator.setProgress((int) bytesWritten);
                lastBytesWritten = bytesWritten;
                lastTotalSize = totalSize;
            }

            @Override
            public void onFinish() {
                super.onFinish();
                userTokensAPICall();
            }
        };

        RequestParams requestParams = new RequestParams();
        requestParams.put("user_id", userID);
        requestParams.put("fuel_type", selectedFuelType);
        requestParams.put("fuel_quantity", fuelQuantity);
        requestParams.put("total_cost", totalCost);
        requestParams.put("payment_type", PAYMENT_TYPE);

        Log.i("REACH", "user_id available - " + requestParams.has("user_id"));

        hinjuAPIClient.get(WalletCheckOutActivity.this, "payment/process", requestParams, httpResponseHandler);
    }

    private void postProcessPaymentAPICall(int statusCode, String responseJSONString) {
        JSONObject responseJSONObject;
        JSONObject hinjuJSONObject;
        JSONObject processPaymentResponseJSONObject;
        JSONObject tokenJSONObject;

        try {
            responseJSONObject = new JSONObject(responseJSONString);
            hinjuJSONObject = responseJSONObject.getJSONObject("hinju");
            processPaymentResponseJSONObject = hinjuJSONObject.getJSONObject("process_payment_response");
            tokenJSONObject = processPaymentResponseJSONObject.getJSONObject("token");

            if (statusCode == 200) {
                transactionComplete = true;

                SharedPreferences.Editor editor = session.edit();
                editor.putInt(getString(R.string.sp_wallet), processPaymentResponseJSONObject.getInt("wallet"));
                editor.apply();

                balanceTextView.setText(getString(R.string.wallet_balance_text, decimalFormat.format(processPaymentResponseJSONObject.getInt("wallet"))));

                String unit = "litre";
                if (fuelQuantity > 1)
                    unit = "litres";

                String tokenString = tokenJSONObject.getString("token");
                String fuelString  = tokenJSONObject.getString("name");
                String dateString = tokenJSONObject.getString("date_created");
                String costString = decimalFormat.format(totalCost);

                cardTokenTextView.setText(tokenString);
                cardCostTextView.setText(getString(R.string.card_cost_text, costString));
                cardFuelTextView.setText(getString(R.string.card_fuel_text, fuelString, fuelQuantity, unit));
                cardDateCreatedTextView.setText(getString(R.string.card_date_created_text, relativeTimeString(dateStringToLong(dateString))));

                generatedToken = tokenString;

            } else if (statusCode == 400) {

                new MaterialAlertDialogBuilder(this)
                        .setTitle(processPaymentResponseJSONObject.getString("code"))
                        .setMessage(processPaymentResponseJSONObject.getString("message"))
                        .setPositiveButton("OK", (dialog, which) -> {

                        })
                        .show();
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
            public void onFinish() {
                super.onFinish();
                progressIndicator.setVisibility(View.INVISIBLE);
                processPaymentMaterialButton.setCheckable(true);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                progressIndicator.setMin(0);
                progressIndicator.setMax((int) (totalSize + lastTotalSize));
                progressIndicator.setProgress((int) (bytesWritten + lastBytesWritten));
            }
        };
        RequestParams requestParams = new RequestParams();
        requestParams.put("user_id", userID);

        hinjuAPIClient.get(WalletCheckOutActivity.this, "user/tokens", requestParams, httpResponseHandler);
    }

    private void postUserTokensAPICall(int statusCode, String responseJSONString) {
        if (statusCode == 200) {
            SharedPreferences.Editor editor = session.edit();
            editor.putString(getString(R.string.sp_user_tokens_json_string), responseJSONString);
            editor.apply();

            Log.i("PREFERENCE SAVED", "postUserTokensAPICall shared preference saved successfully");

            materialCardView.setVisibility(View.VISIBLE);

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