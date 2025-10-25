package com.prm392.g5.labverse.activity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.prm392.g5.labverse.R;
import com.prm392.g5.labverse.activity.handleAnnotation.ExportAnnotationActivity;
import com.prm392.g5.labverse.activity.readPaper.PrepareViewPdfActivity;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paper.AddPaperRequest;
import com.prm392.g5.labverse.dto.paper.AddPaperResponse;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.repository.PaperRepository;
import com.prm392.g5.labverse.util.PdfMetadataUtil;
import com.prm392.g5.labverse.util.S3Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportPaperActivity extends AppCompatActivity {

    private PaperRepository paperRepository = new PaperRepository();
    private ActivityResultLauncher<String> pickPdfLauncher;
    private File uploadedFile;
    private String s3Key;

    private Paper paper;

    //todo cần link với giao diện của TA
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_import_paper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Đăng ký launcher chọn file PDF
        pickPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        copyUriToTempFile(uri);
                    }
                });

        //todo the line below is for test, delete it when it is not neccessary
        AppDatabase.databaseWriteExecutor.execute(() -> AppDatabase.getInstance(this).paperAnnotationDao().getAnnotationById("446795ba-6574-4319-9181-0630911a899e"));

        Button btnPick = findViewById(R.id.btnPickPdf);
        btnPick.setOnClickListener(v -> pickPdfLauncher.launch("application/pdf"));

        //todo test open pdf
        String paperId = "ad069cc3-e0d5-45df-bf7d-153614965dcb";
        Button btnOpenPaper = findViewById(R.id.btnOpenPdf);
        btnOpenPaper.setOnClickListener(v -> PrepareViewPdfActivity.open(this, paperId));

        //todo test export annotation
        Button btnExportAnnotationButton = findViewById(R.id.btnExportAnnotation);
        btnExportAnnotationButton.setOnClickListener(v -> ExportAnnotationActivity.open(this, paperId));
    }


    private void copyUriToTempFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String fileName = getFileName(uri);

            //the s3Key is simmilar to the location of the destination file
            s3Key = SharePreferenceManager.getInstance().getUserId() + "/paper/" + fileName;
            uploadedFile = new File(getExternalFilesDir(null), s3Key);

            // Tạo thư mục cha nếu chưa có
            File parentDir = uploadedFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // tạo đầy đủ cả cây thư mục
            }

            //viết ra file đích
            try (FileOutputStream outputStream = new FileOutputStream(uploadedFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            //file location được dùng làm s3 key luôn
            //hơi liều nhưng để xử lí nhanh thì đành làm thế
            uploadToS3();
        } catch (Exception e) {
            Log.e("Upload", "Copy file lỗi", e);
        }
    }

    private String getFileName(Uri uri) {
        String name = null;

        // Nếu là content:// (DocumentProvider), Thử lấy từ ContentResolver (DISPLAY_NAME)
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    name = cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
        }

        // Nếu là file:// hoặc Uri không có metadata, tách tên từ URI path
        if (name == null) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                name = (cut != -1) ? path.substring(cut + 1) : path;
            } else {
                name = "temp.pdf";
            }
        }
        //Sanitize toàn bộ tên trước (tránh ký tự lạ ảnh hưởng đến substring)
        // \p{L}   tất cả ký tự chữ cái Unicode (Latin có dấu, Nhật, Trung, Hàn, v.v.)
        // \p{N}   tất cả chữ số Unicode
        // ._-   vẫn giữ lại dấu chấm, gạch dưới, gạch ngang như trước
        // [^...]   nghĩa là bất kỳ ký tự nào KHÔNG thuộc nhóm này sẽ bị thay bằng _
        name = name.replaceAll("[^\\p{L}\\p{N}._-]", "_");

        return name;
    }

    private void uploadToS3() {
        paper = new Paper();

        //lấy metadata của file, total page,  citation, category... vào trước khi upload file
        getFileMetadata();

        if (uploadedFile == null) return;

        // get url để upload
        paperRepository.getUploadUrl(s3Key, new Callback<>() {
            @Override
            public void onResponse(Call<S3SignedUrlResponse> call, Response<S3SignedUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String uploadUrl = response.body().getUrl();
                    //upload lên url của s3
                    S3Util.uploadPdfToS3(uploadUrl, uploadedFile, new S3Util.UploadCallback() {
                        @Override
                        public void onSuccess() {
                            paper.setS3Key(s3Key); //lưu lại key
                            runOnUiThread(() -> Toast.makeText(ImportPaperActivity.this, "Upload thành công", Toast.LENGTH_SHORT).show());
                            // tiếp tục xử lí gửi metadata cho remote, rồi lưu về local database
                            sendBackPaperMetadata();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("S3", "Lỗi lúc lấy url", e);
                            runOnUiThread(() -> Toast.makeText(ImportPaperActivity.this, "Lỗi lúc lấy url: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    });
                } else {
                    Toast.makeText(ImportPaperActivity.this, "Lấy URL upload thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<S3SignedUrlResponse> call, Throwable t) {
                Toast.makeText(ImportPaperActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFileMetadata() {
        //todo này dành cho ai làm phần citation với suggestion xử lí nè
        //lấy title, authorname... rồi là category các thứ đó
        //set vào các properties của paper nha
        //category thì kệ Vũ nhé, chưa biết xử lí như nào :vv

        //chỗ này viết thử thôi nha, TA có thể xem lại chỗ này, chưa xử lí trường hợp ko lấy được metadata đâu
        //lấy xong rồi thì set lại giá trị vào cái paper object cho chị nhé
        PdfMetadataUtil.PdfInfo info = PdfMetadataUtil.extractMetadata(this, uploadedFile);
        paper.setTotalPage(info.pageCount);


    }

    private void sendBackPaperMetadata() {
        //aka add Paper object to server
        AddPaperRequest requestDto = new AddPaperRequest();
        requestDto.setS3Key(s3Key);
        requestDto.setTotalPage(paper.getTotalPage());
        //todo tạm thời để cho đủ object request, sau em TA làm thì xem lại nhé
        requestDto.setAuthorName("");
        requestDto.setPublicationYear("");
        requestDto.setTitle("");
        requestDto.setDoi("");

        paperRepository.addPaper(requestDto, new Callback<AddPaperResponse>() {
            @Override
            public void onResponse(Call<AddPaperResponse> call, Response<AddPaperResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String paperId = response.body().getId();
                    paper.setId(paperId);

                    //lưu object paper vào trong local db
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        PaperDao dao = AppDatabase.getInstance(ImportPaperActivity.this).paperDao();
                        dao.insert(paper);
                        Log.d("PDF", "Saved paper: " + paperId);
                    });

                } else {
                    Toast.makeText(ImportPaperActivity.this, "Thêm paper vào server thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AddPaperResponse> call, Throwable t) {
                Toast.makeText(ImportPaperActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}