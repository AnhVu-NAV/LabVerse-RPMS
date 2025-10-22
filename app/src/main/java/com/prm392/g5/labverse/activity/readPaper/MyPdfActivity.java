package com.prm392.g5.labverse.activity.readPaper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.util.AnnotationHelper;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.configuration.page.PageScrollMode;
import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.ui.PdfActivity;
import com.pspdfkit.ui.PdfActivityIntentBuilder;
import com.pspdfkit.ui.PdfFragment;

import java.io.File;


public class MyPdfActivity extends PdfActivity {

    private PdfFragment fragment;
    private PdfDocument document;
    private AnnotationHelper annotationHelper;
    private static Paper openedPaper;
    private static PaperAnnotation openedAnnotation;
    private static File localAnnotationFile;
    private static File localPdfFile;

//    public static void open(Context context, File pdfFile, File annotationFile, Paper paper) {
    public static void open(Context context, Paper paper, PaperAnnotation paperAnnotation,  File pdfFile, File annotationFile) {

        openedPaper = paper;
        openedAnnotation = paperAnnotation;
        localPdfFile = pdfFile;
        localAnnotationFile = annotationFile;

        PdfActivityConfiguration config = new PdfActivityConfiguration.Builder(context)
                .annotationEditingEnabled(true)
                .autosaveEnabled(false)
                .scrollDirection(PageScrollDirection.VERTICAL)
                .scrollMode(PageScrollMode.CONTINUOUS)
                .pageNumberOverlayEnabled(true)
                .build();

        Intent intent = PdfActivityIntentBuilder
                .fromUri(context, Uri.fromFile(localPdfFile))
                .configuration(config)
                .build();
        intent.setClass(context, MyPdfActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //mở Activity này trong một task mới, không gắn vào task hiện tại
        context.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Gọi Nutrient inflate menu mặc định trước
        super.onCreateOptionsMenu(menu);

        // Ghi log ra xem trong menu có những item gì
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            Log.d("MyPdfActivity", "Menu item: " + item.getTitle());
        }

        // Xoá theo tên hiển thị
        for (int i = menu.size() - 1; i >= 0; i--) {  // lặp ngược để xoá an toàn
            MenuItem item = menu.getItem(i);
            CharSequence title = item.getTitle();
            if (title != null &&
                    (
//                            title.toString().contains("Settings") ||
//                            title.toString().contains("Document Info") ||
                            title.toString().contains("Share")
                    )
            ) {
                menu.removeItem(item.getItemId());
            }
        }

        //chèn thêm item
        menu.add(Menu.NONE, R.id.menu_upload, Menu.NONE, "Upload")
                .setIcon(R.drawable.ic_launcher_foreground)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); // => hiển thị trong menu 3 chấm
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    //gọi sau khi menu và toolbar đã sẵn sàng, trước khi menu hiển thị
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_upload) {
            Toast.makeText(this, "Upload clicked!", Toast.LENGTH_SHORT).show();
            // Gọi hàm bạn muốn ở đây
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDocumentLoaded(@NonNull PdfDocument document) {
        super.onDocumentLoaded(document);

//        // Dò tìm Toolbar trong layout của Nutrient
//        Toolbar toolbar = findToolbarInHierarchy();
//        if (toolbar != null) {
//            toolbar.setBackgroundColor(Color.parseColor("#1E1E1E"));
//
//            ImageButton uploadButton = new ImageButton(this);
//            uploadButton.setImageResource(R.drawable.ic_launcher_foreground);
//            uploadButton.setBackgroundColor(Color.TRANSPARENT);
//            toolbar.addView(uploadButton);
//
//            uploadButton.setOnClickListener(v -> exportAnnotation());
//        } else {
//            Toast.makeText(this, "Toolbar not found!", Toast.LENGTH_SHORT).show();
//        }

        //sau khi document load xong sẽ thực hiện đống việc sau đây
        this.document = document;
        this.fragment = getPdfFragment(); // Lấy fragment hiện tại
        this.annotationHelper = new AnnotationHelper(document);

        //Import annotation local overlay lên PDF
        if (localAnnotationFile.exists()) {
            annotationHelper.importFromLocal(localAnnotationFile);
        }

        // restore last page
        if (openedPaper.getCurrentPage() >= 0) {
            fragment.setPageIndex(openedPaper.getCurrentPage(), true);
        }

    }

//    private Toolbar findToolbarInHierarchy() {
//        ViewGroup root = findViewById(android.R.id.content);
//        return findToolbarRecursively(root);
//    }
//
//    private Toolbar findToolbarRecursively(ViewGroup parent) {
//        for (int i = 0; i < parent.getChildCount(); i++) {
//            View child = parent.getChildAt(i);
//            if (child instanceof Toolbar) {
//                return (Toolbar) child;
//            } else if (child instanceof ViewGroup) {
//                Toolbar toolbar = findToolbarRecursively((ViewGroup) child);
//                if (toolbar != null) return toolbar;
//            }
//        }
//        return null;
//    }
//
//    private void exportAnnotation() {
//        Log.d("toolbar", "Asns nut moiw them vaof tool bar");
//    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fragment == null) return;

        int currentPage = fragment.getPageIndex();

        // Save last page
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PaperDao dao = AppDatabase.getInstance(this).paperDao();
            Paper paper = dao.getById(openedPaper.getId());
            if (paper != null) {
                paper.setCurrentPage(currentPage);
                dao.update(paper);
                Log.d("PDF", "Saved last page: " + currentPage);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (document == null || annotationHelper == null) return;
        //lưu lại annotation và upload lên server
        //hiện tại đang bắt lưu lại annotation (dù cho không thay đổi), vì bản dùng thử không xem được cái là annotation có bị thay đổi hay không
        try {
            //document.saveIfModified(); //cái này là cho embeded hết những cái chỉnh sửa trên pdf vào file pdf nè

            File parentDir = localAnnotationFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            annotationHelper.updateAnnotation(localAnnotationFile, openedAnnotation);

            //todo update cả cái last page aka current page của paper lên nữa
        } catch (Exception e) {
            Log.e("MyPdfActivity", "Save annotation failed", e);
            Toast.makeText(this, "Sync annotation fail", Toast.LENGTH_LONG).show();
        }
    }

    /** Gọi khi người dùng muốn xuất file JSON annotation */
//    public void exportAnnotationFile() {
//        File localFile = ensureLocalAnnotationFile();
//        annotationManager.exportToLocal(localFile);
//        Log.d("MyPdfActivity", "Annotation file exported to: " + localFile.getPath());
//    }

}
