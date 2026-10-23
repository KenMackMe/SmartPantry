package com.example.smartpantry.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.MatchResult;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the Suggested Recipes screen.
 *
 * <p>The same adapter serves both tabs. For the "Almost there" tab,
 * {@code showMissing} is true and each row also lists the one missing ingredient.</p>
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<MatchResult> results = new ArrayList<>();
    private final Listener listener;
    private boolean showMissing;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setResults(List<MatchResult> newResults, boolean showMissing) {
        results.clear();
        results.addAll(newResults);
        this.showMissing = showMissing;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final MatchResult result = results.get(position);
        final Recipe recipe = result.getRecipe();
        Context context = holder.itemView.getContext();

        holder.name.setText(recipe.getName());
        holder.meta.setText(context.getString(R.string.recipe_meta,
                recipe.getIngredients().size(), recipe.getMinutes()));

        if (showMissing && !result.getMissing().isEmpty()) {
            List<String> names = new ArrayList<>();
            for (RecipeIngredient missing : result.getMissing()) {
                names.add(UiUtils.describeIngredient(missing));
            }
            holder.missing.setText(context.getString(R.string.missing_label,
                    TextUtils.join(", ", names)));
            holder.missing.setVisibility(View.VISIBLE);
        } else {
            holder.missing.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final TextView missing;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_recipe_name);
            meta = itemView.findViewById(R.id.text_recipe_meta);
            missing = itemView.findViewById(R.id.text_missing);
        }
    }
}
