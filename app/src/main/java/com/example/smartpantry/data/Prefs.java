package com.example.smartpantry.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Thin wrapper around SharedPreferences for the two Settings toggles. */
public final class Prefs {

    /** "Expiring soon" means within this many days from today. */
    public static final int EXPIRY_WARNING_DAYS = 3;

    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_IGNORE_EXPIRED = "ignore_expired";

    private Prefs() {
        // Static utility class.
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Show the expiring-soon banner on the pantry screen (default: on). */
    public static boolean expiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    /** Skip expired items when matching recipes (default: off, so matching is purely quantity based). */
    public static boolean ignoreExpired(Context context) {
        return prefs(context).getBoolean(KEY_IGNORE_EXPIRED, false);
    }

    public static void setIgnoreExpired(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_IGNORE_EXPIRED, enabled).apply();
    }
}
