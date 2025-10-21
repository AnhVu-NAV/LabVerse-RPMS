package com.prm392.g5.labverse.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.prm392.g5.labverse.R;
import java.util.Calendar;

public class AdvancedFilterBottomSheet extends BottomSheetDialogFragment {

    private EditText etFilterAuthor;
    private EditText etFilterJournal;
    private EditText etFilterTag;
    private LinearLayout llFilterYear;
    private TextView tvFilterYear;
    private Button btnReset;
    private Button btnApply;

    private String selectedYear = "";
    private OnFilterAppliedListener listener;

    public interface OnFilterAppliedListener {
        void onFilterApplied(String author, String journal, String tag, String year);
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_advanced_filter, container, false);

        initializeViews(view);
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        etFilterAuthor = view.findViewById(R.id.et_filter_author);
        etFilterJournal = view.findViewById(R.id.et_filter_journal);
        etFilterTag = view.findViewById(R.id.et_filter_tag);
        llFilterYear = view.findViewById(R.id.ll_filter_year);
        tvFilterYear = view.findViewById(R.id.tv_filter_year);
        btnReset = view.findViewById(R.id.btn_reset);
        btnApply = view.findViewById(R.id.btn_apply);
    }

    private void setupListeners() {
        // Year picker
        llFilterYear.setOnClickListener(v -> showYearPicker());

        // Reset button
        btnReset.setOnClickListener(v -> resetFilters());

        // Apply button
        btnApply.setOnClickListener(v -> applyFilters());
    }

    private void showYearPicker() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        // Create a DatePickerDialog for year selection
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                selectedYear = String.valueOf(year);
                tvFilterYear.setText(selectedYear);
                tvFilterYear.setTextColor(getResources().getColor(R.color.search_text, null));
            },
            currentYear,
            0,
            1
        );

        // Hide day and month pickers (show only year)
        datePickerDialog.getDatePicker().findViewById(
            getResources().getIdentifier("day", "id", "android")
        ).setVisibility(View.GONE);
        datePickerDialog.getDatePicker().findViewById(
            getResources().getIdentifier("month", "id", "android")
        ).setVisibility(View.GONE);

        datePickerDialog.setTitle(getString(R.string.filter_year_label));
        datePickerDialog.show();
    }

    private void resetFilters() {
        etFilterAuthor.setText("");
        etFilterJournal.setText("");
        etFilterTag.setText("");
        tvFilterYear.setText(getString(R.string.filter_year_hint));
        tvFilterYear.setTextColor(getResources().getColor(R.color.search_hint, null));
        selectedYear = "";

        Toast.makeText(requireContext(), "Filters reset", Toast.LENGTH_SHORT).show();
    }

    private void applyFilters() {
        String author = etFilterAuthor.getText().toString().trim();
        String journal = etFilterJournal.getText().toString().trim();
        String tag = etFilterTag.getText().toString().trim();
        String year = selectedYear;

        // Check if at least one filter is applied
        if (author.isEmpty() && journal.isEmpty() && tag.isEmpty() && year.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter at least one filter", Toast.LENGTH_SHORT).show();
            return;
        }

        // Notify listener
        if (listener != null) {
            listener.onFilterApplied(author, journal, tag, year);
        }

        // Show confirmation
        StringBuilder message = new StringBuilder("Filters applied:");
        if (!author.isEmpty()) message.append("\nAuthor: ").append(author);
        if (!journal.isEmpty()) message.append("\nJournal: ").append(journal);
        if (!tag.isEmpty()) message.append("\nTag: ").append(tag);
        if (!year.isEmpty()) message.append("\nYear: ").append(year);

        Toast.makeText(requireContext(), message.toString(), Toast.LENGTH_LONG).show();

        // Dismiss the bottom sheet
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make the bottom sheet full width
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}

