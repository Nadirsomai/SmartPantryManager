package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.text.NumberFormat;
import java.util.List;

public class RecipeIngredientAdapter
        extends RecyclerView.Adapter<RecipeIngredientAdapter.IngredientViewHolder> {

    private final List<RecipeIngredient> ingredients;
    private final NumberFormat quantityFormat = NumberFormat.getNumberInstance();

    public RecipeIngredientAdapter(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
        quantityFormat.setMaximumFractionDigits(2);
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_ingredient, parent, false);
        return new IngredientViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        RecipeIngredient ingredient = ingredients.get(position);
        holder.nameText.setText(ingredient.getName());
        holder.quantityText.setText(holder.itemView.getContext().getString(
                R.string.pantry_item_quantity,
                quantityFormat.format(ingredient.getQuantity()),
                ingredient.getUnit()));
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameText;
        private final TextView quantityText;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_recipe_ingredient_name);
            quantityText = itemView.findViewById(R.id.text_recipe_ingredient_quantity);
        }
    }
}
