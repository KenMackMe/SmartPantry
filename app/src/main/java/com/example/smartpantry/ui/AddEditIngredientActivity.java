package com.example.smartpantry.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.smartpantry.R;
import com.example.smartpantry.data.DatabaseHelper;
import com.example.smartpantry.logic.IngredientNormalizer;
import com.example.smartpantry.logic.Units;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;

/**
 * One form for BOTH adding a new ingredient and editing an existing one.
 *
 * <p>Mode is decided by the Intent: if it carries {@link #EXTRA_ITEM_ID} the
 * screen loads that row and saves with UPDATE; otherwise it saves with INSERT.</p>
 */
public class AddEditIngredientActivity extends BaseActivity {

    /** Intent extra holding the id of the pantry row to edit (absent = add mode). */
    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private static final String STATE_EXPIRY = "state_expiry";
    private static final int MAX_NAME_LENGTH = 40;
    private static final double MAX_QUANTITY = 100000;

    private DatabaseHelper db;
    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private TextInputEditText editExpiry;
    private Spinner spinnerUnit;
    private Button buttonClearDate;

    private long itemId = -1;
    private boolean editMode;
    /** Chosen expiry as ISO text (yyyy-MM-dd), or null when none is set. */
    private String selectedExpiry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        db = DatabaseHelper.getInstance(this);
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        editMode = itemId != -1;
        setupToolbar(editMode ? R.string.title_edit_ingredient : R.string.title_add_ingredient, true);

        layoutName = findViewById(R.id.layout_name);
        layoutQuantity = findViewById(R.id.layout_quantity);
        editName = findViewById(R.id.edit_name);
        editQuantity = findViewById(R.id.edit_quantity);
        editExpiry = findViewById(R.id.edit_expiry);
        spinnerUnit = findViewById(R.id.spinner_unit);
        buttonClearDate = findViewById(R.id.button_clear_date);
        TextInputLayout layoutExpiry = findViewById(R.id.layout_expiry);

        // Spinner: an ArrayAdapter feeds the unit choices from res/values/strings.xml.
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.unit_options, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        // Date picker opens from the field or from its calendar icon.
        editExpiry.setOnClickListener(v -> showDatePicker());
        layoutExpiry.setEndIconOnClickListener(v -> showDatePicker());
        buttonClearDate.setOnClickListener(v -> {
            selectedExpiry = null;
            updateExpiryField();
        });
        findViewById(R.id.button_save).setOnClickListener(v -> saveIngredient());

        if (savedInstanceState != null) {
            // Rotation: text fields and the spinner restore themselves; the date is ours.
            selectedExpiry = savedInstanceState.getString(STATE_EXPIRY);
        } else if (editMode) {
            loadExistingItem();
        }
        updateExpiryField();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_EXPIRY, selectedExpiry);
    }

    /** Edit mode: READ the row and pre-fill the form. */
    private void loadExistingItem() {
        PantryItem item = db.getPantryItem(itemId);
        if (item == null) {
            Toast.makeText(this, R.string.error_item_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        editName.setText(item.getName());
        editQuantity.setText(Units.formatQuantity(item.getQuantity()));
        selectedExpiry = item.getExpiry();

        for (int i = 0; i < Units.DISPLAY_UNITS.length; i++) {
            if (Units.DISPLAY_UNITS[i].equalsIgnoreCase(item.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        LocalDate initial = UiUtils.parseDate(selectedExpiry);
        if (initial == null) {
            initial = LocalDate.now();
        }
        // DatePickerDialog months are 0-based (January = 0), LocalDate months are 1-based.
        new DatePickerDialog(this, (picker, year, month, day) -> {
            selectedExpiry = LocalDate.of(year, month + 1, day).toString();   // ISO yyyy-MM-dd
            updateExpiryField();
        }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
    }

    private void updateExpiryField() {
        LocalDate date = UiUtils.parseDate(selectedExpiry);
        editExpiry.setText(date == null ? "" : UiUtils.formatDate(date));
        buttonClearDate.setVisibility(date == null ? View.GONE : View.VISIBLE);
    }

    /** Validates the form; on success performs INSERT (add) or UPDATE (edit). */
    private void saveIngredient() {
        layoutName.setError(null);
        layoutQuantity.setError(null);

        String name = textOf(editName).replaceAll("\\s+", " ");
        String quantityText = textOf(editQuantity).replace(',', '.');   // allow "1,5"
        boolean valid = true;

        // ---- name ----
        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.error_name_empty));
            valid = false;
        } else if (name.length() > MAX_NAME_LENGTH) {
            layoutName.setError(getString(R.string.error_name_long));
            valid = false;
        } else if (IngredientNormalizer.normalize(name).isEmpty()) {
            layoutName.setError(getString(R.string.error_name_letters));
            valid = false;
        }

        // ---- quantity ----
        double quantity = 0;
        if (quantityText.isEmpty()) {
            layoutQuantity.setError(getString(R.string.error_quantity_empty));
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
            } catch (NumberFormatException e) {
                quantity = Double.NaN;
            }
            if (Double.isNaN(quantity) || Double.isInfinite(quantity)) {
                layoutQuantity.setError(getString(R.string.error_quantity_invalid));
                valid = false;
            } else if (quantity <= 0) {
                layoutQuantity.setError(getString(R.string.error_quantity_positive));
                valid = false;
            } else if (quantity > MAX_QUANTITY) {
                layoutQuantity.setError(getString(R.string.error_quantity_large));
                valid = false;
            }
        }

        if (!valid) {
            return;     // errors are now shown under the offending fields
        }

        String unit = (String) spinnerUnit.getSelectedItem();
        PantryItem item = new PantryItem(editMode ? itemId : -1, name, quantity, unit, selectedExpiry);

        if (editMode) {
            if (db.updatePantryItem(item) == 0) {
                Toast.makeText(this, R.string.error_item_not_found, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.item_updated, Toast.LENGTH_SHORT).show();
            }
        } else {
            db.insertPantryItem(item);
            Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_OK);
        finish();
    }

    private String textOf(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    // ---- toolbar menu (delete is only offered when editing) ----

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (editMode) {
            getMenuInflater().inflate(R.menu.menu_edit_ingredient, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_item_title)
                .setMessage(getString(R.string.delete_item_message, textOf(editName)))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deletePantryItem(itemId);
                    Toast.makeText(this, getString(R.string.item_deleted, textOf(editName)),
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
