package com.prm392.g5.labverse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.activity.handleAnnotation.ExportAnnotationActivity;
import com.prm392.g5.labverse.activity.handleAnnotation.ImportAnnotationActivity;

public class AnnotationsFragment extends Fragment {

    private static final String ARG_PAPER_ID = "paper_id";
    private String paperId;

    public AnnotationsFragment() {
        // Required empty public constructor
    }

    public static AnnotationsFragment newInstance(String paperId) {
        Bundle args = new Bundle();
        args.putString(ARG_PAPER_ID, paperId);
        AnnotationsFragment fragment = new AnnotationsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AnnotationsFragment newInstance() {
        return new AnnotationsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_annotations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nhận paperId từ bundle
        if (getArguments() != null) {
            paperId = getArguments().getString(ARG_PAPER_ID);
        }

        // TODO test export annotation
        Button btnExportAnnotationButton = view.findViewById(R.id.btnExportAnnotation);
        btnExportAnnotationButton.setOnClickListener(v ->
                ExportAnnotationActivity.open(requireContext(), paperId)
        );

        // TODO test import annotation
        Button btnImportAnnotationButton = view.findViewById(R.id.btnImportAnnotation);
        btnImportAnnotationButton.setOnClickListener(v ->
                ImportAnnotationActivity.open(requireContext(), paperId)
        );
    }
}

