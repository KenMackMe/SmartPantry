package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.data.Prefs;
import com.example.smartpantry.logic.MatchResult;
import com.example.smartpantry.logic.PantryStock;
import com.example.smartpantry.logic.RecipeMatcher;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.model.Recipe;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the strict-matching rule against the CURRENT pantry and lists only the
 * recipes the user can cook right now.
 *
 * <p>Two tabs: "Ready to cook" (strict matches - the real suggestions) and the
 * optional "Almost there" (missing exactly one ingredient). They are kept in
 * separate tabs so the strict list is never mixed with partial matches.</p>
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private TabLayout tabLayout;
    private RecipeAdapter adapter;
    private RecyclerView recycler;
    private View emptyContainer;
    private TextView emptyText;
    private TextView almostBanner;
    private Button openPantryButton;

    private List<MatchResult> strictResults = new ArrayList<>();
    private List<MatchResult> almostResults = new ArrayList<>();
    private List<MatchResult> allResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setupToolbar(R.string.title_recipes, false);
        setupBottomNav(R.id.nav_recipes);

        db = DatabaseHelper.getInstance(this);
        tabLayout = findViewById(R.id.tab_layout);
        recycler = findViewById(R.id.recycler_recipes);
        emptyContainer = findViewById(R.id.empty_container);
        emptyText = findViewById(R.id.empty_text);
        almostBanner = findViewById(R.id.almost_banner);
        openPantryButton = findViewById(R.id.button_open_pantry);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        recycler.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_ready, 0)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_almost, 0)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_all, 0)));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                render();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        openPantryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PantryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    /** Re-run the matching whenever the screen is shown, so pantry changes are reflected at once. */
    @Override
    protected void onResume() {
        super.onResume();
        runMatching();
    }

    private void runMatching() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> recipes = db.getAllRecipes();

        PantryStock stock = PantryStock.from(pantry, Prefs.ignoreExpired(this), LocalDate.now());
        strictResults = RecipeMatcher.strictMatches(recipes, stock);
        almostResults = RecipeMatcher.almostThere(recipes, stock);
        allResults = new ArrayList<>();
        for (Recipe recipe : recipes) {
            allResults.add(new MatchResult(recipe, new ArrayList<>()));
        }

        TabLayout.Tab readyTab = tabLayout.getTabAt(0);
        TabLayout.Tab almostTab = tabLayout.getTabAt(1);
        if (readyTab != null) {
            readyTab.setText(getString(R.string.tab_ready, strictResults.size()));
        }
        if (almostTab != null) {
            almostTab.setText(getString(R.string.tab_almost, almostResults.size()));
        }
        TabLayout.Tab allTab = tabLayout.getTabAt(2);
        if (allTab != null) {
            allTab.setText(getString(R.string.tab_all, allResults.size()));
        }
        render();
    }

    /** Shows the list (or the friendly empty message) for the selected tab. */
    private void render() {
        int selectedTab = tabLayout.getSelectedTabPosition();
        boolean almostTabSelected = selectedTab == 1;
        boolean allTabSelected = selectedTab == 2;
        // All Recipes must render the full catalog rather than strict matches.
        List<MatchResult> shown = allTabSelected ? allResults
                : (almostTabSelected ? almostResults : strictResults);

        adapter.setResults(shown, almostTabSelected);
        almostBanner.setVisibility(almostTabSelected ? View.VISIBLE : View.GONE);

        boolean empty = shown.isEmpty();
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (allTabSelected) {
            emptyText.setText(R.string.no_catalog_recipes);
        } else {
            emptyText.setText(almostTabSelected ? R.string.no_almost_recipes : R.string.no_recipes_match);
        }
        openPantryButton.setVisibility((almostTabSelected || allTabSelected) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
