package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final OnRecipeClickListener recipeClickListener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener recipeClickListener) {
        this.recipes = recipes;
        this.recipeClickListener = recipeClickListener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.nameText.setText(recipe.getName());
        holder.descriptionText.setText(recipe.getDescription());
        holder.itemView.setOnClickListener(view -> recipeClickListener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameText;
        private final TextView descriptionText;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_recipe_name);
            descriptionText = itemView.findViewById(R.id.text_recipe_description);
        }
    }
}
