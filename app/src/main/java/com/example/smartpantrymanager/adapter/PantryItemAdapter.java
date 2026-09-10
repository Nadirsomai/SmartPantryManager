package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.logic.ExpiryChecker;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class PantryItemAdapter
        extends RecyclerView.Adapter<PantryItemAdapter.PantryItemViewHolder> {

    public interface OnPantryItemClickListener {
        void onPantryItemClick(PantryItem pantryItem);
    }

    private final List<PantryItem> pantryItems = new ArrayList<>();
    private final NumberFormat quantityFormat = NumberFormat.getNumberInstance();
    private final OnPantryItemClickListener itemClickListener;
    private boolean expiryAlertsEnabled;
    private int expiryWarningDays;

    public PantryItemAdapter(OnPantryItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        quantityFormat.setMaximumFractionDigits(2);
    }

    public void setPantryItems(List<PantryItem> updatedItems) {
        int previousItemCount = pantryItems.size();
        pantryItems.clear();
        if (previousItemCount > 0) {
            notifyItemRangeRemoved(0, previousItemCount);
        }
        pantryItems.addAll(updatedItems);
        if (!updatedItems.isEmpty()) {
            notifyItemRangeInserted(0, updatedItems.size());
        }
    }

    public void setExpiryWarningSettings(boolean enabled, int warningDays) {
        expiryAlertsEnabled = enabled;
        expiryWarningDays = warningDays;
    }

    @NonNull
    @Override
    public PantryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryItemViewHolder holder, int position) {
        PantryItem pantryItem = pantryItems.get(position);
        holder.nameText.setText(pantryItem.getName());
        holder.quantityText.setText(holder.itemView.getContext().getString(
                R.string.pantry_item_quantity,
                quantityFormat.format(pantryItem.getQuantity()),
                pantryItem.getUnit()));
        bindExpiryStatus(holder, pantryItem);
        holder.itemView.setOnClickListener(view -> itemClickListener.onPantryItemClick(pantryItem));
    }

    private void bindExpiryStatus(PantryItemViewHolder holder, PantryItem pantryItem) {
        int textColor = R.color.muted_text;
        String expiryText = holder.itemView.getContext().getString(
                R.string.pantry_item_expiry, pantryItem.getExpiryDate());

        if (expiryAlertsEnabled) {
            ExpiryChecker.ExpiryStatus status = ExpiryChecker.getStatus(
                    pantryItem.getExpiryDate(), expiryWarningDays, new Date());
            if (status == ExpiryChecker.ExpiryStatus.EXPIRED) {
                textColor = R.color.error_red;
                expiryText = holder.itemView.getContext().getString(
                        R.string.pantry_item_expired, pantryItem.getExpiryDate());
            } else if (status == ExpiryChecker.ExpiryStatus.EXPIRING_SOON) {
                long daysLeft = ExpiryChecker.getDaysUntilExpiry(
                        pantryItem.getExpiryDate(), new Date());
                textColor = R.color.gold_light;
                expiryText = holder.itemView.getContext().getResources().getQuantityString(
                        R.plurals.pantry_item_days_left,
                        (int) daysLeft,
                        pantryItem.getExpiryDate(),
                        daysLeft);
            }
        }

        holder.expiryText.setText(expiryText);
        holder.expiryText.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(), textColor));
    }

    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    static class PantryItemViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameText;
        private final TextView quantityText;
        private final TextView expiryText;

        PantryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_pantry_item_name);
            quantityText = itemView.findViewById(R.id.text_pantry_item_quantity);
            expiryText = itemView.findViewById(R.id.text_pantry_item_expiry);
        }
    }
}
