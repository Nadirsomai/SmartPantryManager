package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.PantryItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class PantryItemAdapter
        extends RecyclerView.Adapter<PantryItemAdapter.PantryItemViewHolder> {

    public interface OnPantryItemClickListener {
        void onPantryItemClick(PantryItem pantryItem);
    }

    private final List<PantryItem> pantryItems = new ArrayList<>();
    private final NumberFormat quantityFormat = NumberFormat.getNumberInstance();
    private final OnPantryItemClickListener itemClickListener;

    public PantryItemAdapter(OnPantryItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        quantityFormat.setMaximumFractionDigits(2);
    }

    public void setPantryItems(List<PantryItem> updatedItems) {
        pantryItems.clear();
        pantryItems.addAll(updatedItems);
        notifyDataSetChanged();
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
        holder.expiryText.setText(holder.itemView.getContext().getString(
                R.string.pantry_item_expiry,
                pantryItem.getExpiryDate()));
        holder.itemView.setOnClickListener(view -> itemClickListener.onPantryItemClick(pantryItem));
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
