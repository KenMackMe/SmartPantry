package com.example.smartpantry.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.data.Prefs;
import com.google.android.material.switchmaterial.SwitchMaterial;

/** Settings: two preference toggles (SharedPreferences) and a "clear pantry" action. */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupToolbar(R.string.title_settings, false);
        setupBottomNav(R.id.nav_settings);

        // Toggle 1: expiring-soon banner on the pantry screen.
        SwitchMaterial alertsSwitch = findViewById(R.id.switch_expiry_alerts);
        alertsSwitch.setChecked(Prefs.expiryAlertsEnabled(this));
        alertsSwitch.setOnCheckedChangeListener((button, checked) ->
                Prefs.setExpiryAlertsEnabled(this, checked));
        ((TextView) findViewById(R.id.text_expiry_alerts_desc)).setText(
                getString(R.string.setting_expiry_alerts_desc, Prefs.EXPIRY_WARNING_DAYS));

        // Toggle 2: should expired items be ignored by the recipe matcher?
        SwitchMaterial ignoreSwitch = findViewById(R.id.switch_ignore_expired);
        ignoreSwitch.setChecked(Prefs.ignoreExpired(this));
        ignoreSwitch.setOnCheckedChangeListener((button, checked) ->
                Prefs.setIgnoreExpired(this, checked));

        findViewById(R.id.button_clear_pantry).setOnClickListener(v -> confirmClearPantry());
    }

    private void confirmClearPantry() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_pantry_title)
                .setMessage(R.string.clear_pantry_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    DatabaseHelper.getInstance(this).deleteAllPantryItems();
                    Toast.makeText(this, R.string.pantry_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
