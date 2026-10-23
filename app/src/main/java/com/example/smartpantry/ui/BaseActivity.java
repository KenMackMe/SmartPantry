package com.example.smartpantry.ui;

import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantry.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Behaviour shared by every screen: the toolbar and the bottom navigation bar.
 *
 * <p>Navigation rules:
 * <ul>
 *   <li>The Pantry screen is the "home" of the back stack.</li>
 *   <li>Tapping "Pantry" in the bar uses CLEAR_TOP | SINGLE_TOP so the existing
 *       Pantry Activity is reused instead of stacking copies.</li>
 *   <li>Tapping another tab from a non-Pantry screen finishes the current one,
 *       so the back button always returns to the Pantry.</li>
 * </ul>
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Which bottom-navigation item belongs to this screen (0 = none). */
    private int currentNavItemId = 0;

    /** Installs the layout's Toolbar as this Activity's action bar. */
    protected void setupToolbar(@StringRes int titleRes, boolean showUpButton) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(titleRes);
            bar.setDisplayHomeAsUpEnabled(showUpButton);
        }
    }

    /** Wires the bottom navigation bar and highlights this screen's tab. */
    protected void setupBottomNav(final int currentItemId) {
        currentNavItemId = currentItemId;
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.getMenu().findItem(currentItemId).setChecked(true);

        nav.setOnItemSelectedListener(item -> {
            int target = item.getItemId();
            if (target != currentNavItemId) {
                navigateTo(target);
            }
            return true;
        });
    }

    private void navigateTo(int targetItemId) {
        if (targetItemId == R.id.nav_pantry) {
            Intent intent = new Intent(this, PantryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return;
        }

        Class<?> target = (targetItemId == R.id.nav_recipes)
                ? SuggestedRecipesActivity.class
                : SettingsActivity.class;
        startActivity(new Intent(this, target));

        // Keep the back stack as [Pantry, current tab] rather than a long chain.
        if (currentNavItemId != R.id.nav_pantry) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When the user comes BACK to this screen, the bar may still show the tab
        // they tapped last; re-highlight the tab that belongs to this screen.
        if (currentNavItemId != 0) {
            BottomNavigationView nav = findViewById(R.id.bottom_nav);
            nav.getMenu().findItem(currentNavItemId).setChecked(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
