package com.example.smartpantry.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.data.Prefs;
import com.example.smartpantry.logic.PantryStock;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.time.LocalDate;

/**
 * Shows one recipe in full. The recipe id arrives through an Intent extra.
 * Each ingredient is ticked (green) or crossed (red) depending on whether the
 * pantry covers it, using the same rule as the suggestions screen.
 */
public class RecipeDetailActivity extends BaseActivity {

    /** Intent extra holding the id of the recipe to display. */
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        setupToolbar(R.string.title_recipe_detail, true);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = db.getRecipe(recipeId);

        if (recipe == null) {
            Toast.makeText(this, R.string.error_recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.getName());
        }

        PantryStock stock = PantryStock.from(db.getAllPantryItems(),
                Prefs.ignoreExpired(this), LocalDate.now());

        ((TextView) findViewById(R.id.text_meta)).setText(getString(R.string.detail_meta,
                recipe.getMinutes(), recipe.getIngredients().size()));

        // ---- ingredient rows ----
        LinearLayout container = findViewById(R.id.container_ingredients);
        int missingCount = 0;
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            boolean have = stock.covers(ingredient);
            if (!have) {
                missingCount++;
            }

            View row = getLayoutInflater().inflate(R.layout.item_recipe_ingredient, container, false);
            ImageView status = row.findViewById(R.id.icon_status);
            status.setImageResource(have ? R.drawable.ic_check : R.drawable.ic_close);
            status.setContentDescription(getString(have ? R.string.cd_have : R.string.cd_missing));
            ((TextView) row.findViewById(R.id.text_ingredient))
                    .setText(UiUtils.capitalize(ingredient.getName()));
            ((TextView) row.findViewById(R.id.text_amount))
                    .setText(UiUtils.amount(ingredient.getQuantity(), ingredient.getUnit()));
            container.addView(row);
        }

        // ---- status banner ----
        TextView statusBanner = findViewById(R.id.text_status);
        if (missingCount == 0) {
            statusBanner.setText(R.string.detail_ready);
            statusBanner.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_light));
            statusBanner.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        } else {
            statusBanner.setText(getString(R.string.detail_missing, missingCount));
            statusBanner.setBackgroundColor(ContextCompat.getColor(this, R.color.banner_warning_bg));
            statusBanner.setTextColor(ContextCompat.getColor(this, R.color.expiring_amber));
        }

        // ---- method ----
        ((TextView) findViewById(R.id.text_steps)).setText(recipe.getSteps());
    }
}
