package com.prm392.g5.labverse.activity.readPaper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

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
    public void onDocumentLoaded(@NonNull PdfDocument document) {
        super.onDocumentLoaded(document);

        //sau khi document load xong sẽ thực hiện đống việc sau đây
        this.document = document;
        this.fragment = getPdfFragment(); // Lấy fragment hiện tại
        this.annotationHelper = new AnnotationHelper(document);

        //Import annotation local overlay lên PDF
        if (localAnnotationFile.exists()) {
            annotationHelper.importFromLocal(localAnnotationFile);
        }

        restoreLastPage();

    }

    /**
     * Lưu vị trí trang hiện tại + tổng số trang
     */
    private void restoreLastPage() {
        int totalPages = document.getPageCount();
        Log.d("PDF", "Tổng số trang: " + totalPages);
        // restore last page
        if (openedPaper.getCurrentPage() >= 0) {
            fragment.setPageIndex(openedPaper.getCurrentPage(), true);
        }
    }

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
            annotationHelper.exportToLocal(localAnnotationFile);
            Log.d("MyPdfActivity", "Annotation changed → saved to local");
            //upload annotation to remote storage
            annotationHelper.uploadToRemote(localAnnotationFile, openedAnnotation.getAnnotationS3Key());
            //add or update annotation in backend database and device database
            annotationHelper.updateAnnotationInDatabases(openedAnnotation);
            //todo update cả cái last page của paper lên nữa
        } catch (Exception e) {
            Log.e("MyPdfActivity", "Save annotation failed", e);
        }
    }

    /** Gọi khi người dùng muốn xuất file JSON annotation */
//    public void exportAnnotationFile() {
//        File localFile = ensureLocalAnnotationFile();
//        annotationManager.exportToLocal(localFile);
//        Log.d("MyPdfActivity", "Annotation file exported to: " + localFile.getPath());
//    }

}
