package com.samtechs.thehinjuapp.ui.main.home;

import static com.google.firebase.encoders.json.BuildConfig.APPLICATION_ID;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.samtechs.thehinjuapp.R;
import com.samtechs.thehinjuapp.databinding.FragmentHomeBinding;
import com.samtechs.thehinjuapp.ui.payment.ConfirmOrderActivity;
import com.samtechs.thehinjuapp.util.MinMaxFilter;

public class HomeFragment extends Fragment {
    private SharedPreferences session;

    private FragmentHomeBinding binding;

    private static final int PETROL = 100;
    private static final int DIESEL = 200;

    private int selectedFuelType = PETROL;

    private MaterialCardView petrolMaterialCardView;
    private MaterialCardView dieselMaterialCardView;
    private ShapeableImageView petrolFuelTypeShapeableImageView;
    private ShapeableImageView dieselFuelTypeShapeableImageView;
    private FrameLayout petrolFuelTypeFrameLayout;
    private FrameLayout dieselFuelTypeFrameLayout;

    private TextInputLayout fuelQuantityTextInputLayout;
    private TextInputEditText fuelQuantityTextInputEditText;

    private MaterialButton fuelQuantityMaterialButton;

    private int fuelQuantity = 0;

    private int MIN_FUEL_QUANTITY;
    private int MAX_FUEL_QUANTITY;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = requireActivity().getSharedPreferences(APPLICATION_ID + R.string.session_preference, Context.MODE_PRIVATE);
        String minimum_fuel_q = session.getString(getString(R.string.sp_min_fuel_quantity), "0");
        String maximum_fuel_q = session.getString(getString(R.string.sp_max_fuel_quantity), "0");
        MIN_FUEL_QUANTITY = Integer.parseInt(minimum_fuel_q);
        MAX_FUEL_QUANTITY = Integer.parseInt(maximum_fuel_q);

        if (MAX_FUEL_QUANTITY == 0) {
//            requireActivity().finish();
//            requireActivity().overridePendingTransition(0, 0);
//            startActivity(requireActivity().getIntent());
//            requireActivity().overridePendingTransition(0, 0);

            requireActivity().overridePendingTransition(0, 0);
            requireActivity().recreate();
            requireActivity().overridePendingTransition(0, 0);
        }

        petrolMaterialCardView = requireView().findViewById(R.id.petrolFuelTypeCard);
        dieselMaterialCardView = requireView().findViewById(R.id.dieselFuelTypeCard);
        petrolFuelTypeFrameLayout = requireView().findViewById(R.id.petrolFuelTypeFrameLayout);
        dieselFuelTypeFrameLayout = requireView().findViewById(R.id.dieselFuelTypeFrameLayout);
        petrolFuelTypeShapeableImageView = requireView().findViewById(R.id.petrolFuelTypeIcon);
        dieselFuelTypeShapeableImageView = requireView().findViewById(R.id.dieselFuelTypeIcon);

        fuelQuantityTextInputLayout = requireView().findViewById(R.id.fuelQuantityTextInputLayout);
        fuelQuantityTextInputEditText = requireView().findViewById(R.id.fuelQuantityTextInputEditText);
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        fuelQuantityMaterialButton = requireView().findViewById(R.id.fuelQuantityButton);

        petrolMaterialCardView.setOnClickListener(v -> {
            selectedFuelType = PETROL;
            petrolFuelTypeFrameLayout.setBackgroundColor(requireActivity().getResources().getColor(R.color.md_theme_light_primary, null));
            dieselFuelTypeFrameLayout.setBackgroundColor(requireActivity().getResources().getColor(R.color.md_theme_light_onPrimary, null));
            petrolFuelTypeShapeableImageView.setColorFilter(requireActivity().getResources().getColor(R.color.md_theme_light_primary, null));
            dieselFuelTypeShapeableImageView.setColorFilter(requireActivity().getResources().getColor(R.color.md_theme_light_onPrimary, null));
            fuelQuantityTextInputEditText.clearFocus();
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        });

        dieselMaterialCardView.setOnClickListener(v -> {
            selectedFuelType = DIESEL;
            petrolFuelTypeFrameLayout.setBackgroundColor(requireActivity().getResources().getColor(R.color.md_theme_light_onPrimary, null));
            dieselFuelTypeFrameLayout.setBackgroundColor(requireActivity().getResources().getColor(R.color.md_theme_light_primary, null));
            petrolFuelTypeShapeableImageView.setColorFilter(requireActivity().getResources().getColor(R.color.md_theme_light_onPrimary, null));
            dieselFuelTypeShapeableImageView.setColorFilter(requireActivity().getResources().getColor(R.color.md_theme_light_primary, null));
            fuelQuantityTextInputEditText.clearFocus();
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        });

        fuelQuantityTextInputLayout.setHelperText(getString(R.string.fuel_quantity_helper_text, MIN_FUEL_QUANTITY, MAX_FUEL_QUANTITY));
        fuelQuantityTextInputEditText.setFilters(new InputFilter[]{new MinMaxFilter(MIN_FUEL_QUANTITY, MAX_FUEL_QUANTITY)});

        fuelQuantityMaterialButton.setOnClickListener(v -> {
            try {
                //noinspection ConstantConditions
                fuelQuantity = Integer.parseInt(fuelQuantityTextInputEditText.getText().toString());
            } catch (NumberFormatException e) {
                fuelQuantity = 0;
            }
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            fuelQuantityTextInputEditText.clearFocus();

            if (fuelQuantity < MIN_FUEL_QUANTITY || fuelQuantity > MAX_FUEL_QUANTITY) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Invalid Fuel Quantity")
                        .setMessage("Please enter a valid fuel quantity. Please not that the minimum and maximum fuel quantity allowed is " + MIN_FUEL_QUANTITY + " and " + MAX_FUEL_QUANTITY + " litres respectively")
                        .setPositiveButton("OK", (dialog, which) -> {

                        })
                        .show();
            } else {
                Intent intent = new Intent(requireContext(), ConfirmOrderActivity.class);
                intent.putExtra("FUEL_TYPE_EXTRA", selectedFuelType);
                intent.putExtra("FUEL_QUANTITY", fuelQuantity);
                startActivity(intent);
            }

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}