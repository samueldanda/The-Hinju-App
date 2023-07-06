package com.samtechs.thehinjuapp.ui.payment;

import static com.google.firebase.encoders.json.BuildConfig.APPLICATION_ID;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.samtechs.thehinjuapp.R;
import com.samtechs.thehinjuapp.ui.payment.checkout.CreditCardCheckOutActivity;
import com.samtechs.thehinjuapp.ui.payment.checkout.GooglePayCheckOutActivity;
import com.samtechs.thehinjuapp.ui.payment.checkout.PayPalCheckOutActivity;
import com.samtechs.thehinjuapp.ui.payment.checkout.WalletCheckOutActivity;

import java.text.DecimalFormat;
import java.util.Objects;

public class ConfirmOrderActivity extends AppCompatActivity {
    private SharedPreferences session;

    private MaterialCardView paymentMethodCard1;
    private MaterialCardView paymentMethodCard2;
    private MaterialCardView paymentMethodCard3;
    private MaterialCardView paymentMethodCard4;

    private MaterialRadioButton paymentMethodRadioButton1;
    private MaterialRadioButton paymentMethodRadioButton2;
    private MaterialRadioButton paymentMethodRadioButton3;
    private MaterialRadioButton paymentMethodRadioButton4;

    private TextView psFuelTypeNameTextView;
    private TextView psFuelQuantityTextView;
    private TextView psFuelCostTextView;
    private TextView psServiceChargeTextView;
    private TextView psTaxTextView;
    private TextView psTotalCostTextView;

    private MaterialButton confirmOrderButton;

    private static final int CARD1 = 100;
    private static final int CARD2 = 200;
    private static final int CARD3 = 300;
    private static final int CARD4 = 400;

    private static final int PETROL = 100;
    private static final int DIESEL = 200;

    private int selectedPaymentMethod;

    private int selectedFuelType;

    private int fuelQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_confirm_order);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        selectedFuelType = intent.getIntExtra("FUEL_TYPE_EXTRA", 0);
        fuelQuantity = intent.getIntExtra("FUEL_QUANTITY", 0);

        session = getSharedPreferences(APPLICATION_ID + R.string.session_preference, Context.MODE_PRIVATE);
        String minimum_fuel_q = session.getString(getString(R.string.sp_min_fuel_quantity), "0");
        String maximum_fuel_q = session.getString(getString(R.string.sp_max_fuel_quantity), "0");
        String diesel_p = session.getString(getString(R.string.sp_diesel), "0");
        String petrol_p = session.getString(getString(R.string.sp_petrol), "0");
        String tax_per_l = session.getString(getString(R.string.sp_tax_per_litre), "0");
        String service_charge_per_l = session.getString(getString(R.string.sp_service_charge_per_litre), "0");

        int MIN_FUEL_QUANTITY = Integer.parseInt(minimum_fuel_q);
        int MAX_FUEL_QUANTITY = Integer.parseInt(maximum_fuel_q);
        int PETROL_PRICE = Integer.parseInt(petrol_p);
        int DIESEL_PRICE = Integer.parseInt(diesel_p);
        int TAX_PER_LITRE = Integer.parseInt(tax_per_l);
        int SERVICE_CHARGE_PER_LITRE = Integer.parseInt(service_charge_per_l);

        paymentMethodCard1 = findViewById(R.id.paymentMethodCard1);
        paymentMethodCard2 = findViewById(R.id.paymentMethodCard2);
        paymentMethodCard3 = findViewById(R.id.paymentMethodCard3);
        paymentMethodCard4 = findViewById(R.id.paymentMethodCard4);

        paymentMethodRadioButton1 = findViewById(R.id.paymentMethodRadio1);
        paymentMethodRadioButton2 = findViewById(R.id.paymentMethodRadio2);
        paymentMethodRadioButton3 = findViewById(R.id.paymentMethodRadio3);
        paymentMethodRadioButton4 = findViewById(R.id.paymentMethodRadio4);

        psFuelTypeNameTextView = findViewById(R.id.psFuelTypeName);
        psFuelQuantityTextView = findViewById(R.id.psFuelQuantity);
        psFuelCostTextView = findViewById(R.id.psFuelCost);
        psServiceChargeTextView = findViewById(R.id.psChargeCost);
        psTaxTextView = findViewById(R.id.psTaxCost);
        psTotalCostTextView = findViewById(R.id.psFuelTotalCost);

        confirmOrderButton = findViewById(R.id.confirm_order_button);

        paymentMethodRadioButton1.setChecked(true);
        selectedPaymentMethod = CARD1;

        String petrolStringText = getString(R.string.petrol_string_text);
        String dieselStringText = getString(R.string.diesel_string_text);

        if (fuelQuantity < MIN_FUEL_QUANTITY || fuelQuantity > MAX_FUEL_QUANTITY) {
            Toast.makeText(this, "Unknown Error Occurred", Toast.LENGTH_LONG).show();
            onBackPressed();
        } else {
            psFuelQuantityTextView.setText(getString(R.string.ps_fuel_quantity_text, fuelQuantity));
        }

        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
        long fuelCost = 0, serviceCharge, tax, total_cost;

        if (selectedFuelType == PETROL) {
            psFuelTypeNameTextView.setText(petrolStringText);
            fuelCost = (long) fuelQuantity * PETROL_PRICE;
            psFuelCostTextView.setText(getString(R.string.ps_fuel_cost_text, String.format("%10s", decimalFormat.format(fuelCost))));
        } else if (selectedFuelType == DIESEL) {
            psFuelTypeNameTextView.setText(dieselStringText);
            fuelCost = (long) fuelQuantity * DIESEL_PRICE;
            psFuelCostTextView.setText(getString(R.string.ps_fuel_cost_text, String.format("%10s", decimalFormat.format(fuelCost))));
        } else {
            Toast.makeText(this, "Unknown Error Occurred", Toast.LENGTH_LONG).show();
            onBackPressed();
        }

        serviceCharge = (long) fuelQuantity * SERVICE_CHARGE_PER_LITRE;
        psServiceChargeTextView.setText(getString(R.string.ps_fuel_service_charge_text, String.format("%10s", decimalFormat.format(serviceCharge))));

        tax = (long) fuelQuantity * TAX_PER_LITRE;
        psTaxTextView.setText(getString(R.string.ps_fuel_tax_text, String.format("%10s", decimalFormat.format(tax))));

        total_cost = fuelCost + serviceCharge;
        psTotalCostTextView.setText(getString(R.string.ps_fuel_total_cost_text, String.format("%10s", decimalFormat.format(total_cost))));
        confirmOrderButton.setText(getString(R.string.continue_to_pay_button_text, decimalFormat.format(total_cost)));

        paymentMethodCard1.setOnClickListener(v -> {
            selectedPaymentMethod = CARD1;
            paymentMethodRadioButton1.setChecked(true);
            paymentMethodRadioButton2.setChecked(false);
            paymentMethodRadioButton3.setChecked(false);
            paymentMethodRadioButton4.setChecked(false);
        });
        paymentMethodCard2.setOnClickListener(v -> {
            selectedPaymentMethod = CARD2;
            paymentMethodRadioButton1.setChecked(false);
            paymentMethodRadioButton2.setChecked(true);
            paymentMethodRadioButton3.setChecked(false);
            paymentMethodRadioButton4.setChecked(false);
        });
        paymentMethodCard3.setOnClickListener(v -> {
            selectedPaymentMethod = CARD3;
            paymentMethodRadioButton1.setChecked(false);
            paymentMethodRadioButton2.setChecked(false);
            paymentMethodRadioButton3.setChecked(true);
            paymentMethodRadioButton4.setChecked(false);
        });
        paymentMethodCard4.setOnClickListener(v -> {
            selectedPaymentMethod = CARD4;
            paymentMethodRadioButton1.setChecked(false);
            paymentMethodRadioButton2.setChecked(false);
            paymentMethodRadioButton3.setChecked(false);
            paymentMethodRadioButton4.setChecked(true);
        });

        paymentMethodRadioButton1.setOnClickListener(v -> {
            selectedPaymentMethod = CARD1;
            paymentMethodRadioButton2.setChecked(false);
            paymentMethodRadioButton3.setChecked(false);
            paymentMethodRadioButton4.setChecked(false);
        });
        paymentMethodRadioButton2.setOnClickListener(v -> {
            selectedPaymentMethod = CARD2;
            paymentMethodRadioButton1.setChecked(false);
            paymentMethodRadioButton3.setChecked(false);
            paymentMethodRadioButton4.setChecked(false);
        });
        paymentMethodRadioButton3.setOnClickListener(v -> {
            selectedPaymentMethod = CARD3;
            paymentMethodRadioButton1.setChecked(false);
            paymentMethodRadioButton2.setChecked(false);
            paymentMethodRadioButton4.setChecked(false);
        });
        paymentMethodRadioButton4.setOnClickListener(v -> {
            selectedPaymentMethod = CARD4;
            paymentMethodRadioButton1.setChecked(false);
            paymentMethodRadioButton2.setChecked(false);
            paymentMethodRadioButton3.setChecked(false);

        });

        confirmOrderButton.setOnClickListener(v -> {
            if (selectedPaymentMethod == CARD1) {
                Intent intent1 = new Intent(ConfirmOrderActivity.this, WalletCheckOutActivity.class);
                intent1.putExtra("FUEL_TYPE_EXTRA", selectedFuelType);
                intent1.putExtra("FUEL_QUANTITY", fuelQuantity);
                intent1.putExtra("TOTAL_COST", total_cost);
                startActivity(intent1);
            } else if (selectedPaymentMethod == CARD2) {
                Intent intent2 = new Intent(ConfirmOrderActivity.this, PayPalCheckOutActivity.class);
                intent2.putExtra("FUEL_TYPE_EXTRA", selectedFuelType);
                intent2.putExtra("FUEL_QUANTITY", fuelQuantity);
                intent2.putExtra("TOTAL_COST", total_cost);
//                startActivity(intent2);
                showPaymentNotAvailableDialogue("PayPal");
            } else if (selectedPaymentMethod == CARD3) {
                Intent intent3 = new Intent(ConfirmOrderActivity.this, CreditCardCheckOutActivity.class);
                intent3.putExtra("FUEL_TYPE_EXTRA", selectedFuelType);
                intent3.putExtra("FUEL_QUANTITY", fuelQuantity);
                intent3.putExtra("TOTAL_COST", total_cost);
//                startActivity(intent3);
                showPaymentNotAvailableDialogue("Credit card");
            } else if (selectedPaymentMethod == CARD4) {
                Intent intent4 = new Intent(ConfirmOrderActivity.this, GooglePayCheckOutActivity.class);
                intent4.putExtra("FUEL_TYPE_EXTRA", selectedFuelType);
                intent4.putExtra("FUEL_QUANTITY", fuelQuantity);
                intent4.putExtra("TOTAL_COST", total_cost);
//                startActivity(intent4);
                showPaymentNotAvailableDialogue("Google Pay");
            }
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

    void showPaymentNotAvailableDialogue(String paymentMethod) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Payment Method Unavailable")
                .setMessage(paymentMethod + " payment method is not available as it is not in the scope. Please select Account wallet payment method instead")
                .setPositiveButton("OK", (dialog, which) -> {

                })
                .show();
    }
}