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
import com.prm392.g5.labverse.activity.readPaper.PrepareViewPdfActivity;

public class ReadFragment extends Fragment {

    private static final String ARG_PAPER_ID = "arg_paper_id";

    public ReadFragment() {
        // Required empty public constructor
    }

    public static ReadFragment newInstance() {
        return new ReadFragment();
    }

    public static ReadFragment newInstance(@NonNull String paperId) {
        ReadFragment f = new ReadFragment();
        Bundle b = new Bundle();
        b.putString(ARG_PAPER_ID, paperId);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_read, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnOpenPaper = view.findViewById(R.id.btnOpenPdf);
        String paperId = getArguments() != null ? getArguments().getString("arg_paper_id") : null;

        if (btnOpenPaper != null) {
            btnOpenPaper.setOnClickListener(v -> {
                if (paperId != null) {
                    PrepareViewPdfActivity.open(requireContext(), paperId);
                }
            });
        } else {
            // Log để dễ debug nếu layout bị đổi ID/không có nút:
            android.util.Log.w("ReadFragment", "btnOpenPdf not found in fragment_read layout");
        }
    }

}

