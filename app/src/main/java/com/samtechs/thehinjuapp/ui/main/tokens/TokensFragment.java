package com.samtechs.thehinjuapp.ui.main.tokens;

import static com.google.firebase.encoders.json.BuildConfig.APPLICATION_ID;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.samtechs.thehinjuapp.R;
import com.samtechs.thehinjuapp.adapters.TokenCardListAdapter;
import com.samtechs.thehinjuapp.apputils.HinjuAPIClient;
import com.samtechs.thehinjuapp.apputils.InternetConnectivity;
import com.samtechs.thehinjuapp.databinding.FragmentTokensBinding;
import com.samtechs.thehinjuapp.objects.TokenCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class TokensFragment extends Fragment {
    private SharedPreferences session;

    private LinearProgressIndicator progressIndicator;

    private InternetConnectivity internetConnectivity;

    private Button refreshIconButton;
    private TextView noTokenTextView;
    private ScrollView tokenScrollView;

    private static final int DEFAULT_SELECTED_CARD_INDEX = 0;

    private String userID;
    private Integer noOfTokens;

    private TokenCard[] tokenCards;
    private RecyclerView tokenListRecyclerView;
    private TokenCardListAdapter tokenCardListAdapter;
    private TokenCardListAdapter.MyClickListener myClickListener;

    private FragmentTokensBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(TokensViewModel.class);

        binding = FragmentTokensBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = requireActivity().getSharedPreferences(APPLICATION_ID + R.string.session_preference, Context.MODE_PRIVATE);
        String responseJSONString = session.getString(getString(R.string.sp_user_tokens_json_string), "0");
        userID = session.getString(getString(R.string.sp_user_id), "0");

        progressIndicator = view.findViewById(R.id.linear_progress_indicator);
        refreshIconButton = view.findViewById(R.id.refreshIconButton);
        tokenListRecyclerView = view.findViewById(R.id.token_list_recycler_view);
        noTokenTextView = view.findViewById(R.id.noTokenTextView);
        tokenScrollView = view.findViewById(R.id.tokenScrollView);

        progressIndicator.setVisibility(View.INVISIBLE);

        setTokenCardsData(responseJSONString);
        initiateTokenCardListAdapter();

        myClickListener = new TokenCardListAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position) {
                cardItemClick(position);
            }

            @Override
            public void onTokenCardTopLayoutClick(int position) {
                int lastSelected = tokenCardListAdapter.getSelected();
                if (lastSelected != position) {
                    tokenCardListAdapter.setSelected(position);
                    tokenCardListAdapter.notifyItemChanged(position);
                    tokenCardListAdapter.notifyItemChanged(lastSelected);
                }
            }

            @Override
            public void onCopyButtonClick(String token) {
                cardCopyButtonClick(token);
            }

            @Override
            public void onShareButtonClick(String token) {
                cardShareButtonClick(token);
            }
        };
        tokenCardListAdapter.setListener(myClickListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        tokenListRecyclerView.setLayoutManager(layoutManager);
        tokenListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        tokenListRecyclerView.setAdapter(tokenCardListAdapter);

        refreshIconButton.setOnClickListener(v -> {
            internetConnectivity = new InternetConnectivity(requireContext());
            if (!internetConnectivity.isConnected()) {

                Snackbar.make(refreshIconButton, R.string.internet_connection_unavailable_text, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.md_theme_light_error))
                        .show();

                return;
            }

            userTokensAPICall();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void cardItemClick(int position) {
        Toast.makeText(requireContext(), "Card: " + position, Toast.LENGTH_LONG).show();
        System.out.println("Card: " + position);
    }

    private void cardCopyButtonClick(String token) {
        ClipboardManager clipboard = (ClipboardManager)
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("token", token);
        clipboard.setPrimaryClip(clip);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            Toast.makeText(requireContext(), "Copied", Toast.LENGTH_LONG).show();
    }

    private void cardShareButtonClick(String token) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, token);
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    private void initiateTokenCardListAdapter() {
        tokenCardListAdapter = new TokenCardListAdapter();
        tokenCardListAdapter.setContext(requireContext());
        tokenCardListAdapter.setNoOfTokens(noOfTokens);
        tokenCardListAdapter.setTokenCards(tokenCards);
        tokenCardListAdapter.setSelected(DEFAULT_SELECTED_CARD_INDEX);
    }

    private void initiateTokenCardListAdapter2() {
        int lastNoOfTokens = tokenCardListAdapter.getNoOfTokens();
        if (lastNoOfTokens != noOfTokens) {
            tokenCardListAdapter.setSelected(DEFAULT_SELECTED_CARD_INDEX);
            tokenCardListAdapter.setNoOfTokens(noOfTokens);
        }
        tokenCardListAdapter.setTokenCards(tokenCards);
        tokenCardListAdapter.notifyItemRangeChanged(0, noOfTokens);
    }

    private void setTokenCardsData(String responseJSONString) {
        JSONObject responseJSONObject;
        JSONObject hinjuJSONObject;
        JSONObject userTokensResponseJSONObject;
        JSONArray tokensJSONArray;
        JSONObject tokenJSONObject;

        try {
            responseJSONObject = new JSONObject(responseJSONString);
            hinjuJSONObject = responseJSONObject.getJSONObject("hinju");
            userTokensResponseJSONObject = hinjuJSONObject.getJSONObject("user_tokens_response");

            noOfTokens = userTokensResponseJSONObject.getInt("no_of_token");

            if (noOfTokens > 0) {
                noTokenTextView.setVisibility(View.GONE);
                tokenScrollView.setVisibility(View.VISIBLE);
                tokenCards = new TokenCard[noOfTokens];
                tokensJSONArray = userTokensResponseJSONObject.getJSONArray("tokens");
                for (int i = 0; i < noOfTokens; i++) {
                    tokenJSONObject = tokensJSONArray.getJSONObject(i);
                    tokenCards[i] = new TokenCard();
                    tokenCards[i].setTokenID(tokenJSONObject.getString("token_id"));
                    tokenCards[i].setToken(tokenJSONObject.getString("token"));
                    tokenCards[i].setFuelAmount(String.valueOf(tokenJSONObject.getInt("fuel_amount")));
                    tokenCards[i].setFuelName(tokenJSONObject.getString("name"));
                    tokenCards[i].setTotalCost(String.valueOf(tokenJSONObject.getInt("cost")));
                    tokenCards[i].setDateCreated(tokenJSONObject.getString("date_created"));
                    tokenCards[i].setDateUsed(tokenJSONObject.getString("date_used"));
                    tokenCards[i].setUsed(tokenJSONObject.getString("used").compareTo("YES") == 0);
                }
            }
            else {
                noTokenTextView.setVisibility(View.VISIBLE);
                tokenScrollView.setVisibility(View.GONE);
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
                progressIndicator.setVisibility(View.VISIBLE);
                refreshIconButton.setClickable(false);
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
                refreshIconButton.setClickable(true);
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                progressIndicator.setMin(0);
                progressIndicator.setMax((int) totalSize);
                progressIndicator.setProgress((int) bytesWritten);
            }
        };
        RequestParams requestParams = new RequestParams();
        requestParams.put("user_id", userID);

        hinjuAPIClient.get(requireContext(), "user/tokens", requestParams, httpResponseHandler);
    }

    private void postUserTokensAPICall(int statusCode, String responseJSONString) {
        if (statusCode == 200) {
            SharedPreferences.Editor editor = session.edit();
            editor.putString(getString(R.string.sp_user_tokens_json_string), responseJSONString);
            editor.apply();

            Log.i("PREFERENCE SAVED", "postUserTokensAPICall shared preference saved successfully");

            setTokenCardsData(responseJSONString);
            initiateTokenCardListAdapter2();

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