package com.prm392.g5.labverse.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.prm392.g5.labverse.R;

public class CitationFragment extends Fragment {

    private TextView tvCitationContent;
    private Button btnCopyApa;
    private Button btnCopyMla;
    private Button btnEdit;
    private Button btnCopyBibtex;

    // Sample citation data
    private String authorName = "Dr. Amelia Harper";
    private String paperTitle = "The Impact of AI on Education";
    private String journalName = "Journal of Educational Technology";
    private String year = "2023";
    private String doi = "10.1234/edtech.2023.0001";

    public CitationFragment() {
        // Required empty public constructor
    }

    public static CitationFragment newInstance() {
        return new CitationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_citation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        displayCitation();
    }

    private void initViews(View view) {
        tvCitationContent = view.findViewById(R.id.tv_citation_content);
        btnCopyApa = view.findViewById(R.id.btn_copy_apa);
        btnCopyMla = view.findViewById(R.id.btn_copy_mla);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnCopyBibtex = view.findViewById(R.id.btn_copy_bibtex);
    }

    private void setupListeners() {
        btnCopyApa.setOnClickListener(v -> copyToClipboard(formatAPA(), getString(R.string.citation_copied_apa)));
        btnCopyMla.setOnClickListener(v -> copyToClipboard(formatMLA(), getString(R.string.citation_copied_mla)));
        btnCopyBibtex.setOnClickListener(v -> copyToClipboard(formatBibTeX(), getString(R.string.citation_copied_bibtex)));
        btnEdit.setOnClickListener(v -> openEditDialog());
    }

    private void displayCitation() {
        String citation = authorName + ", \"" + paperTitle + "\", " +
                         journalName + ", " + year + ", DOI: " + doi;
        tvCitationContent.setText(citation);
    }

    private String formatAPA() {
        // APA Format: Author. (Year). Title. Journal, DOI
        return authorName + ". (" + year + "). " + paperTitle + ". " +
               journalName + ", DOI: " + doi;
    }

    private String formatMLA() {
        // MLA Format: Author. "Title." Journal Year: DOI
        return authorName + ". \"" + paperTitle + ".\" " +
               journalName + " " + year + ": " + doi;
    }

    private String formatBibTeX() {
        // BibTeX Format
        String key = authorName.split(" ")[authorName.split(" ").length - 1].toLowerCase() + year;
        return "@article{" + key + ",\n" +
               "  author = {" + authorName + "},\n" +
               "  title = {" + paperTitle + "},\n" +
               "  journal = {" + journalName + "},\n" +
               "  year = {" + year + "},\n" +
               "  doi = {" + doi + "}\n" +
               "}";
    }

    private void copyToClipboard(String text, String message) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("citation", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void openEditDialog() {
        // Create BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_citation, null);
        bottomSheetDialog.setContentView(dialogView);

        // Initialize dialog views
        TextView tvOriginalCitation = dialogView.findViewById(R.id.tv_original_citation);
        EditText etCitationInput = dialogView.findViewById(R.id.et_citation_input);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        ImageView btnBack = dialogView.findViewById(R.id.btn_back);

        // Display current citation
        String currentCitation = authorName + ", \"" + paperTitle + "\", " +
                                journalName + ", " + year + ", DOI: " + doi;
        tvOriginalCitation.setText(currentCitation);
        etCitationInput.setText(currentCitation);

        // Set cursor to end of text
        etCitationInput.setSelection(etCitationInput.getText().length());

        // Back button listener
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        // Cancel button listener
        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Save button listener
        btnSave.setOnClickListener(v -> {
            String newCitation = etCitationInput.getText().toString().trim();

            if (newCitation.isEmpty()) {
                Toast.makeText(getContext(), R.string.citation_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse the citation (simplified parsing)
            parseCitation(newCitation);

            // Update display
            displayCitation();

            // Show success message
            Toast.makeText(getContext(), R.string.citation_updated, Toast.LENGTH_SHORT).show();

            // Dismiss dialog
            bottomSheetDialog.dismiss();
        });

        // Show dialog
        bottomSheetDialog.show();
    }

    private void parseCitation(String citation) {
        // This is a simplified parser. In a real app, you'd want more robust parsing
        // For now, just update the raw citation text
        // You could implement more sophisticated parsing logic here

        // Try to extract author name (text before first comma or quote)
        int firstComma = citation.indexOf(',');
        int firstQuote = citation.indexOf('"');

        if (firstComma > 0 && (firstQuote < 0 || firstComma < firstQuote)) {
            authorName = citation.substring(0, firstComma).trim();
            citation = citation.substring(firstComma + 1).trim();
        }

        // Try to extract title (text between quotes)
        int openQuote = citation.indexOf('"');
        int closeQuote = citation.indexOf('"', openQuote + 1);
        if (openQuote >= 0 && closeQuote > openQuote) {
            paperTitle = citation.substring(openQuote + 1, closeQuote).trim();
            citation = citation.substring(closeQuote + 1).trim();
        }

        // Try to extract year (4 consecutive digits)
        String[] parts = citation.split("[,\\s]+");
        for (String part : parts) {
            if (part.matches("\\d{4}")) {
                year = part;
                break;
            }
        }

        // Try to extract DOI
        int doiIndex = citation.toLowerCase().indexOf("doi:");
        if (doiIndex >= 0) {
            doi = citation.substring(doiIndex + 4).trim();
            // Remove trailing punctuation
            doi = doi.replaceAll("[,;.]+$", "");
        }

        // Try to extract journal (text before year, after title)
        // This is approximate - you may want more sophisticated logic
        String[] journalParts = citation.split(",");
        if (journalParts.length > 0) {
            String potentialJournal = journalParts[0].trim();
            if (!potentialJournal.isEmpty() && !potentialJournal.matches("\\d{4}")
                && !potentialJournal.toLowerCase().startsWith("doi")) {
                journalName = potentialJournal;
            }
        }
    }
}

