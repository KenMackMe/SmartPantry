package com.example.smartpantry.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.Prefs;
import com.example.smartpantry.model.PantryItem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter that binds the pantry rows (from SQLite) to the
 * item_pantry.xml layout.
 *
 * <p>How it works: the RecyclerView asks {@link #onCreateViewHolder} for a few
 * row views, then calls {@link #onBindViewHolder} to fill a row with the data
 * at a position, re-using views as the user scrolls.</p>
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    /** Callbacks implemented by PantryActivity. */
    public interface Listener {
        void onItemClick(PantryItem item);

        void onDeleteClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private LocalDate today = LocalDate.now();

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the data shown and refreshes the list. */
    public void setItems(List<PantryItem> newItems, LocalDate today) {
        items.clear();
        items.addAll(newItems);
        this.today = today;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PantryItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(UiUtils.capitalize(item.getName()));
        holder.quantity.setText(UiUtils.amount(item.getQuantity(), item.getUnit()));
        bindExpiry(holder.expiry, context, item);

        holder.deleteButton.setContentDescription(
                context.getString(R.string.cd_delete_item, item.getName()));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    /** Chooses the expiry wording and colour: red = expired, amber = soon, grey = fine. */
    private void bindExpiry(TextView view, Context context, PantryItem item) {
        LocalDate expiry = item.getExpiryDate();
        String text;
        int colorRes;

        if (expiry == null) {
            text = context.getString(R.string.no_expiry);
            colorRes = R.color.text_secondary;
        } else if (expiry.isBefore(today)) {
            text = context.getString(R.string.expired_on, UiUtils.formatDate(expiry));
            colorRes = R.color.expired_red;
        } else {
            long days = ChronoUnit.DAYS.between(today, expiry);
            if (days == 0) {
                text = context.getString(R.string.expires_today);
                colorRes = R.color.expiring_amber;
            } else if (days == 1) {
                text = context.getString(R.string.expires_in_one_day);
                colorRes = R.color.expiring_amber;
            } else if (days <= Prefs.EXPIRY_WARNING_DAYS) {
                text = context.getString(R.string.expires_in_days, (int) days);
                colorRes = R.color.expiring_amber;
            } else {
                text = context.getString(R.string.expires_on, UiUtils.formatDate(expiry));
                colorRes = R.color.text_secondary;
            }
        }
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(context, colorRes));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the child views of one row so they are looked up only once. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final ImageButton deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name);
            quantity = itemView.findViewById(R.id.text_quantity);
            expiry = itemView.findViewById(R.id.text_expiry);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}
