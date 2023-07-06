package com.samtechs.thehinjuapp.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.samtechs.thehinjuapp.R;
import com.samtechs.thehinjuapp.objects.TokenCard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TokenCardListAdapter extends RecyclerView.Adapter<TokenCardListAdapter.ViewHolder> {
    private TokenCard[] tokenCards;

    private Context context;
    private MyClickListener listener;

    private Integer noOfTokens;
    private Integer selected;

    public TokenCardListAdapter() {
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final LinearLayout tokenCardTopLinearLayout;
        public final LinearLayout tokenCardBottomLinearLayout;
        public final LinearLayout dateUsedLinearLayout;

        public final TextView cardTokenTextView;
        public final Chip cardChip;

        public final TextView cardFuelTextView;
        public final TextView cardCostTextView;
        public final TextView cardDateCreatedTextView;
        public final TextView cardDateUsedTextView;

        public final Button copyTokenIconButton;
        public final Button shareTokenIconButton;

        public ViewHolder(@NonNull View view) {
            super(view);
            tokenCardTopLinearLayout = view.findViewById(R.id.tokenCardTop);
            tokenCardBottomLinearLayout = view.findViewById(R.id.tokenCardBottom);
            dateUsedLinearLayout = view.findViewById(R.id.dateUsedLinearLayout);

            cardTokenTextView = view.findViewById(R.id.cardTokenTextView);
            cardChip = view.findViewById(R.id.cardChip);

            cardFuelTextView = view.findViewById(R.id.cardFuelTextView);
            cardCostTextView = view.findViewById(R.id.cardCostTextView);
            cardDateCreatedTextView = view.findViewById(R.id.cardDateCreatedTextView);
            cardDateUsedTextView = view.findViewById(R.id.cardDateUsedTextView);

            copyTokenIconButton = view.findViewById(R.id.copyTokenIconButton);
            shareTokenIconButton = view.findViewById(R.id.shareTokenIconButton);
        }

    }

    @NonNull
    @Override
    public TokenCardListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_row_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition();
            listener.onItemClick(position);
        });
        viewHolder.tokenCardTopLinearLayout.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition();
            listener.onTokenCardTopLayoutClick(position);
        });
        viewHolder.copyTokenIconButton.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition();
            String token = tokenCards[position].getToken();
            listener.onCopyButtonClick(token);
        });
        viewHolder.shareTokenIconButton.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition();
            String token = tokenCards[position].getToken();
            listener.onShareButtonClick(token);
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TokenCardListAdapter.ViewHolder holder, int position) {

        String unit = "litre";
        int fuelQuantity = Integer.parseInt(tokenCards[position].getFuelAmount());
        if (fuelQuantity > 1)
            unit = "litres";
        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");

        String tokenString = tokenCards[position].getToken();
        String fuelString  = tokenCards[position].getFuelName();
        String dateCreatedString = tokenCards[position].getDateCreated();
        String dateUsedString = tokenCards[position].getDateUsed();
        String costString = decimalFormat.format(Integer.parseInt(tokenCards[position].getTotalCost()));

        holder.cardTokenTextView.setText(tokenString);
        holder.cardCostTextView.setText(getContext().getString(R.string.card_cost_text, costString));
        holder.cardFuelTextView.setText(getContext().getString(R.string.card_fuel_text, fuelString, fuelQuantity, unit));
        holder.cardDateCreatedTextView.setText(getContext().getString(R.string.card_date_created_text, relativeTimeString(dateStringToLong(dateCreatedString))));
        holder.cardDateUsedTextView.setText(getContext().getString(R.string.card_date_used_text, relativeTimeString(dateStringToLong(dateUsedString))));

        if (tokenCards[position].isUsed()) {
            holder.cardChip.setText(getContext().getString(R.string.card_used_chip_text));
            holder.dateUsedLinearLayout.setVisibility(View.VISIBLE);
        }
        else {
            holder.cardChip.setText(getContext().getString(R.string.card_not_used_chip_text));
            holder.dateUsedLinearLayout.setVisibility(View.GONE);
        }

        if (position == selected) {
            holder.tokenCardBottomLinearLayout.setVisibility(View.VISIBLE);
        }
        else
            holder.tokenCardBottomLinearLayout.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return noOfTokens;
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
        return (String) DateUtils.getRelativeDateTimeString(getContext(), time, DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_TIME);
    }

    public TokenCard[] getTokenCards() {
        return tokenCards;
    }

    public void setTokenCards(TokenCard[] tokenCards) {
        this.tokenCards = tokenCards;
    }

    public Integer getNoOfTokens() {
        return noOfTokens;
    }

    public void setNoOfTokens(Integer noOfTokens) {
        this.noOfTokens = noOfTokens;
    }

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
    }

    public MyClickListener getListener() {
        return listener;
    }

    public void setListener(MyClickListener listener) {
        this.listener = listener;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public interface MyClickListener {
        void onItemClick(int position);

        void onTokenCardTopLayoutClick(int position);

        void onCopyButtonClick(String token);

        void onShareButtonClick(String token);
    }
}
