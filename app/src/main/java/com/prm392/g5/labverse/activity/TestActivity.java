package com.prm392.g5.labverse.activity;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.activity.handleAnnotation.ExportAnnotationActivity;
import com.prm392.g5.labverse.activity.handleAnnotation.ImportAnnotationActivity;
import com.prm392.g5.labverse.activity.readPaper.PrepareViewPdfActivity;
import com.prm392.g5.labverse.config.AppDatabase;

public class TestActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_import_paper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //todo test import annotation
        Button btnPick = findViewById(R.id.btnPickPdf);
        btnPick.setOnClickListener(v -> ImportPaperActivity.open(this));


        //todo the line below is for test, delete it when it is not necessary
        AppDatabase.databaseWriteExecutor.execute(() -> AppDatabase.getInstance(this).paperAnnotationDao().getAnnotationById("446795ba-6574-4319-9181-0630911a899e"));

        //todo test open pdf
        String paperId = "ad069cc3-e0d5-45df-bf7d-153614965dcb";
        Button btnOpenPaper = findViewById(R.id.btnOpenPdf);
        btnOpenPaper.setOnClickListener(v -> PrepareViewPdfActivity.open(this, paperId));

        //todo test export annotation
        Button btnExportAnnotationButton = findViewById(R.id.btnExportAnnotation);
        btnExportAnnotationButton.setOnClickListener(v -> ExportAnnotationActivity.open(this, paperId));

        //todo test import annotation
        Button btnImportAnnotationButton = findViewById(R.id.btnImportAnnotation);
        btnImportAnnotationButton.setOnClickListener(v -> ImportAnnotationActivity.open(this, paperId));
    }
}
