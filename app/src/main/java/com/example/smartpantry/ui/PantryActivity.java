package com.example.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.data.Prefs;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Launcher screen: lists every pantry item (READ), and lets the user open an
 * item to edit (UPDATE), delete it (DELETE), or add a new one (CREATE).
 */
public class PantryActivity extends BaseActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView emptyView;
    private TextView expiryBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        setupToolbar(R.string.title_pantry, false);
        setupBottomNav(R.id.nav_pantry);

        db = DatabaseHelper.getInstance(this);
        emptyView = findViewById(R.id.empty_view);
        expiryBanner = findViewById(R.id.expiry_banner);

        RecyclerView recycler = findViewById(R.id.recycler_pantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    /** Reload every time the screen becomes visible, so edits made elsewhere show up. */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        LocalDate today = LocalDate.now();

        adapter.setItems(items, today);
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        updateExpiryBanner(items, today);
    }

    /** Shows "2 expiring within 3 days · 1 expired" when the alerts setting is on. */
    private void updateExpiryBanner(List<PantryItem> items, LocalDate today) {
        if (!Prefs.expiryAlertsEnabled(this)) {
            expiryBanner.setVisibility(View.GONE);
            return;
        }

        int expired = 0;
        int expiringSoon = 0;
        LocalDate soonLimit = today.plusDays(Prefs.EXPIRY_WARNING_DAYS);
        for (PantryItem item : items) {
            LocalDate date = item.getExpiryDate();
            if (date == null) {
                continue;
            }
            if (date.isBefore(today)) {
                expired++;
            } else if (!date.isAfter(soonLimit)) {
                expiringSoon++;
            }
        }

        List<String> parts = new ArrayList<>();
        if (expiringSoon > 0) {
            parts.add(getString(R.string.banner_expiring, expiringSoon, Prefs.EXPIRY_WARNING_DAYS));
        }
        if (expired > 0) {
            parts.add(getString(R.string.banner_expired, expired));
        }

        if (parts.isEmpty()) {
            expiryBanner.setVisibility(View.GONE);
        } else {
            expiryBanner.setText("⚠ " + TextUtils.join(getString(R.string.banner_separator), parts));
            expiryBanner.setVisibility(View.VISIBLE);
        }
    }

    // ---- PantryAdapter.Listener ----

    @Override
    public void onItemClick(PantryItem item) {
        // Explicit Intent: open the form in "edit" mode by passing the row id.
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_item_title)
                .setMessage(getString(R.string.delete_item_message, item.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, getString(R.string.item_deleted, item.getName()),
                            Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
