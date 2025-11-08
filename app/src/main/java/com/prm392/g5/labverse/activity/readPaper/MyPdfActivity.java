package com.prm392.g5.labverse.activity.readPaper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.dto.readingStatus.ReadingStatusRequest;
import com.prm392.g5.labverse.dto.readingStatus.ReadingStatusResponse;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.repository.ReadingStatusRepository;
import com.prm392.g5.labverse.util.AnnotationHelper;
import com.prm392.g5.labverse.util.ApiErrorHandler;
import com.pspdfkit.configuration.activity.PdfActivityConfiguration;
import com.pspdfkit.configuration.page.PageScrollDirection;
import com.pspdfkit.configuration.page.PageScrollMode;
import com.pspdfkit.document.PdfDocument;
import com.pspdfkit.ui.PdfActivity;
import com.pspdfkit.ui.PdfActivityIntentBuilder;
import com.pspdfkit.ui.PdfFragment;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyPdfActivity extends PdfActivity {

    private PdfFragment fragment;
    private PdfDocument document;
    private AnnotationHelper annotationHelper;
    private final ReadingStatusRepository readingStatusRepository = new ReadingStatusRepository();
    private static Paper openedPaper;
    private static PaperAnnotation openedAnnotation;
    private static File localAnnotationFile;
    private static File localPdfFile;
    private ReadingStatusRequest readingStatusRequest = new ReadingStatusRequest();

    //    public static void open(Context context, File pdfFile, File annotationFile, Paper paper) {
    public static void open(Context context, Paper paper, PaperAnnotation paperAnnotation, File pdfFile, File annotationFile) {

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
        menu.add(Menu.NONE, R.id.annotation_export, Menu.NONE, "Export Annotation")
                .setIcon(R.drawable.ic_purple_export_annotation)
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
        if (item.getItemId() == R.id.annotation_export) {
//            Toast.makeText(this, "Export annotation clicked!", Toast.LENGTH_SHORT).show();
            Log.d("EXPORT_ANNOTATION", "Export annotation clicked!");
            new AlertDialog.Builder(this)
                    .setTitle("Export Annotation")
                    .setMessage("Do you want to exporting annotation to a file?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        annotationHelper.exportAnnotationFromLocalToFile(localAnnotationFile, new AnnotationHelper.ExportAnnotationCallback() {
                            @Override
                            public void onSuccess(File exportedFile) {
                                showShareDialog(exportedFile);
                            }

                            @Override
                            public void onFail() {
                                new AlertDialog.Builder(MyPdfActivity.this)
                                        .setTitle("Export Annotation")
                                        .setMessage("There is error during export annotation. Please try later")
                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                        .show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
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

        //sau khi document load xong sẽ thực hiện đống việc sau đây
        this.document = document;
        this.fragment = getPdfFragment(); // Lấy fragment hiện tại
        this.annotationHelper = new AnnotationHelper();

        //Import annotation local overlay lên PDF
        if (localAnnotationFile.exists()) {
            annotationHelper.importFromLocal(localAnnotationFile, document);
        }

        //restore last page
        readingStatusRequest.setUserId(SharePreferenceManager.getInstance().getUserId());
        readingStatusRequest.setPaperId(openedPaper.getId());

        readingStatusRepository.getReadingStatusInfo(
                SharePreferenceManager.getInstance().getUserId(),
                openedPaper.getId(),
                new Callback<ReadingStatusResponse>() {
                    @Override
                    public void onResponse(Call<ReadingStatusResponse> call, Response<ReadingStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ReadingStatusResponse readingStatus = response.body();
                            int lastPage = readingStatus.getCurrentPage();
                            fragment.setPageIndex(lastPage, true);

                            //lưu lại để dùng khi đóng pdf
                            readingStatusRequest.setId(readingStatus.getId());
                        }
                        //lỗi thì thôi không làm gì cả, không restore, kệ nutrient nó mở đâu thì mở
                    }

                    @Override
                    public void onFailure(Call<ReadingStatusResponse> call, Throwable t) {
                        //lỗi thì thôi không làm gì cả, không restore, kệ nutrient nó mở đâu thì mở
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fragment == null) return;

        // Save last page
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PaperDao dao = AppDatabase.getInstance(this).paperDao();
            Paper paper = dao.getById(openedPaper.getId());
            if (paper != null) {
                dao.update(paper);
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
            annotationHelper.updateAnnotation(localAnnotationFile, openedAnnotation, document);

        } catch (Exception e) {
            Log.e("MyPdfActivity", "Save annotation failed", e);
            Toast.makeText(this, "Sync annotation fail", Toast.LENGTH_LONG).show();
        }

        //update cả cái last page aka current page của paper lên nữa
//            int currentPage = fragment.getPageIndex();
        readingStatusRequest.setCurrentPage(fragment.getPageIndex());
        readingStatusRepository.createOrUpdateReadingStatus(
                readingStatusRequest,
                new Callback<ReadingStatusResponse>() {
                    @Override
                    public void onResponse(Call<ReadingStatusResponse> call, Response<ReadingStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("UpdateReadingStatus", "Sync reading status successfully");
                            Toast.makeText(MyPdfActivity.this, "Sync reading status successfully", Toast.LENGTH_LONG).show();
                        } else {
                            ApiErrorHandler.handleApiResponseError(MyPdfActivity.this, response, "UpdateReadingStatus");
                        }
                    }

                    @Override
                    public void onFailure(Call<ReadingStatusResponse> call, Throwable t) {
                        ApiErrorHandler.handleNetworkFailure(MyPdfActivity.this, t, "UpdateReadingStatus");
                    }
                });
    }

    public void showShareDialog(File file) {
        Uri uri = FileProvider.getUriForFile(
                MyPdfActivity.this,
                "com.prm392.g5.labverse.provider",
                file
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/json");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        new AlertDialog.Builder(MyPdfActivity.this)
                .setTitle("Export annotation successfully")
                .setMessage("File is saved at:\n" + file.getAbsolutePath())
                .setPositiveButton("Share", (dialog, which) -> {
                    startActivity(Intent.createChooser(shareIntent, "Share annotation"));
                })
                .setNegativeButton("Close", null)
                .show();
    }

}
