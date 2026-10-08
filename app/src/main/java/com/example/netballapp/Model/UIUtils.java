package com.example.netballapp.Model;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class UIUtils {

    public static void showDatePicker(Context context, EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();

        String currentDate = targetEditText.getText().toString();
        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int day = Integer.parseInt(parts[2]);
                calendar.set(year, month, day);
            } catch (Exception e) {
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    targetEditText.setText(date);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public static void showFutureDatePicker(Context context, EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();

        String currentDate = targetEditText.getText().toString();
        if (!currentDate.isEmpty()) {
            try {
                String[] parts = currentDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int day = Integer.parseInt(parts[2]);
                calendar.set(year, month, day);
            } catch (Exception e) {

            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    targetEditText.setText(date);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    /** Shows a validation error on the field's TextInputLayout; clears it as soon as the user edits. */
    public static void fieldError(EditText field, String message) {
        ViewParent parent = field.getParent();
        while (parent != null && !(parent instanceof TextInputLayout)) parent = parent.getParent();
        if (parent == null) { field.setError(message); field.requestFocus(); return; }

        TextInputLayout layout = (TextInputLayout) parent;
        layout.setError(message);
        field.requestFocus();
        if (field.getTag() == null) {
            field.setTag(Boolean.TRUE);
            field.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                public void onTextChanged(CharSequence s, int a, int b, int c) {}
                public void afterTextChanged(Editable s) { layout.setError(null); }
            });
        }
    }

    public static void showMessage(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show();
    }

    public static void showMessage(Activity activity, String message) {
        showMessage(activity.findViewById(android.R.id.content), message);
    }

    /** Friendly network failure message, with a Retry action when the call can be repeated. */
    public static void networkError(View anchor, Runnable retry) {
        Snackbar bar = Snackbar.make(anchor, "Couldn't reach the server. Check your connection.",
                retry == null ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_INDEFINITE);
        if (retry != null) bar.setAction("Retry", v -> retry.run());
        bar.show();
    }

    public static void networkError(Activity activity, Runnable retry) {
        networkError(activity.findViewById(android.R.id.content), retry);
    }

    /** Thin indeterminate bar across the top of the screen while a load is in flight. */
    public static void setLoading(Activity activity, boolean loading) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        View bar = content.findViewWithTag("loading");
        if (bar == null) {
            if (!loading) return;
            LinearProgressIndicator indicator = new LinearProgressIndicator(activity);
            indicator.setIndeterminate(true);
            indicator.setTag("loading");
            content.addView(indicator, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP));
            bar = indicator;
        }
        bar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    /** Haptic tick for meaningful commits (goal logged, substitution made). */
    public static void confirmHaptic(View view) {
        view.performHapticFeedback(Build.VERSION.SDK_INT >= 30
                ? HapticFeedbackConstants.CONFIRM : HapticFeedbackConstants.VIRTUAL_KEY);
    }
}
