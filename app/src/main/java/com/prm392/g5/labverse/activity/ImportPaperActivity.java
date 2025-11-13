package com.prm392.g5.labverse.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.config.SharePreferenceManager;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.dto.S3SignedUrlResponse;
import com.prm392.g5.labverse.dto.paper.AddPaperRequest;
import com.prm392.g5.labverse.dto.paper.AddPaperResponse;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.repository.PaperRepository;
import com.prm392.g5.labverse.util.FileUtil;
import com.prm392.g5.labverse.util.PdfMetadataUtil;
import com.prm392.g5.labverse.util.S3Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImportPaperActivity extends AppCompatActivity {

    private PaperRepository paperRepository;
    private File uploadedFile;
    private String s3Key;
    private Paper paper;

    private static final Pattern DOI_RE = Pattern.compile(
            "\\b10\\.\\d{4,9}/[-._;()/:A-Z0-9]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern ARXIV_RE = Pattern.compile(
            "\\barXiv:\\s*\\d{4}\\.\\d{4,5}(v\\d+)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern YEAR_RE = Pattern.compile("\\b(19|20)\\d{2}\\b");

    private static String clean(String s) {
        if (s == null) return null;
        s = s.trim();
        s = s.replaceAll("\\s+", " ");
        return s.isEmpty() ? null : s;
    }



    public static void open(Context context) {
        Intent intent = new Intent(context, ImportPaperActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paperRepository = new PaperRepository(this);
        // Đăng ký launcher chọn file PDF
        ActivityResultLauncher<String> pickPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        try {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (SecurityException e) {
                            Log.w("Upload", "Không thể giữ quyền đọc cho URI", e);
                        }
                        copyUriToTempFile(uri);
                    }
                });
        pickPdfLauncher.launch("application/pdf");
    }


    private void copyUriToTempFile(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                Log.d("IMPORT_PAPER", "copyUriToTempFile: Không mở được file từ URI");
                throw new IOException("Không mở được file");
            }

            //Xử lí tên file, loại bỏ bất kì kí tự không hợp lệ
            String fileName = getFileName(uri);

            String userId = SharePreferenceManager.getInstance().getUserId();

            //the s3Key is simmilar to the location of the destination file
            s3Key = userId + "/paper/" + fileName;
            uploadedFile = new File(getExternalFilesDir(null), s3Key);

            // Tạo thư mục cha nếu chưa có
            File parentDir = uploadedFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();

            //viết ra file đích
            FileUtil.copyContentFromTo(inputStream, uploadedFile);

            //file location được dùng làm s3 key luôn
            //hơi liều nhưng để xử lí nhanh thì đành làm thế
            uploadToS3();
        } catch (Exception e) {
//            Log.e("IMPORT_PAPER", "Copy file lỗi", e);
            Toast.makeText(this, "Lỗi khi import file PDF", Toast.LENGTH_SHORT).show();
            finish();
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
                name = "temp.pdf"; //call back file ko có tên
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
        try {
            // 0) Mở file bằng PDFBox-Android
            com.tom_roush.pdfbox.pdmodel.PDDocument doc =
                    com.tom_roush.pdfbox.pdmodel.PDDocument.load(uploadedFile);

            // 1) Embedded info/XMP
            com.tom_roush.pdfbox.pdmodel.PDDocumentInformation info = doc.getDocumentInformation();
            String title   = info != null ? clean(info.getTitle()) : null;
            String author  = info != null ? clean(info.getAuthor()) : null;
            String subject = info != null ? clean(info.getSubject()) : null;
            String keywords= info != null ? clean(info.getKeywords()) : null;

            // 1.1) Page count
            int pageCount = doc.getNumberOfPages();
            paper.setTotalPage(pageCount);

            // 2) Strip text 1–2 trang đầu để bắt DOI/arXiv + heuristics tên/author/year
            String firstPagesText = "";
            try {
                com.tom_roush.pdfbox.text.PDFTextStripper stripper =
                        new com.tom_roush.pdfbox.text.PDFTextStripper();
                stripper.setStartPage(1);
                stripper.setEndPage(Math.min(2, pageCount));
                firstPagesText = stripper.getText(doc);
            } catch (Exception ignore) {}

            String doi = null, arxiv = null;
            Integer year = null;

            if (firstPagesText != null) {
                var doiM = DOI_RE.matcher(firstPagesText);
                if (doiM.find()) doi = doiM.group().replaceAll("[\\s)]+$", "");

                var axM = ARXIV_RE.matcher(firstPagesText);
                if (axM.find()) arxiv = axM.group().replaceAll("\\s+", "");

                var yM = YEAR_RE.matcher(firstPagesText);
                if (yM.find()) year = Integer.valueOf(yM.group());
            }

            // 3) Heuristic title nếu thiếu:
            if (title == null) {
                // Lấy dòng đầu tiên “đủ dài” nhưng không phải ALL CAPS tác giả
                String cand = null;
                if (firstPagesText != null) {
                    String[] lines = firstPagesText.split("\\r?\\n");
                    for (String ln : lines) {
                        String t = clean(ln);
                        if (t == null) continue;
                        if (t.length() < 8) continue;
                        // tránh dòng AUTHOR LIST
                        if (t.matches("(?i).*(department|university|institute|laboratory).*")) continue;
                        // tránh headers/footers
                        if (t.matches("(?i).*(arxiv|doi|preprint|received|revised).*")) continue;
                        cand = t;
                        break;
                    }
                }
                title = cand != null ? cand : clean(stripExt(uploadedFile.getName()));
            }

            // 4) Heuristic authors nếu thiếu:
            if (author == null && firstPagesText != null) {
                // Tìm dòng có nhiều dấu phẩy, có nhiều “từ Capitalized”
                String auth = null;
                String[] lines = firstPagesText.split("\\r?\\n");
                for (int i = 0; i < Math.min(lines.length, 20); i++) {
                    String t = clean(lines[i]);
                    if (t == null) continue;
                    if (t.length() > 200) continue;
                    int commas = t.length() - t.replace(",", "").length();
                    if (commas >= 1 && t.matches(".*[A-Z][a-z]+.*")) {
                        auth = t.replaceAll("\\d+|\\*|\\^|\\(|\\)", "").trim(); // bỏ chỉ số/superscripts
                        break;
                    }
                }
                author = auth;
            }

            // 5) Journal/conference (thô sơ): từ Subject/Keywords hoặc dòng chứa “Conference/Journal/Proceedings”
            String journal = null;
            if (subject != null) journal = subject;
            if (journal == null && firstPagesText != null) {
                for (String ln : firstPagesText.split("\\r?\\n")) {
                    String t = clean(ln);
                    if (t == null) continue;
                    if (t.matches("(?i).*(journal|transactions|proceedings|conference|symposium).*")) {
                        journal = t;
                        break;
                    }
                }
            }

            // 6) Gán vào model (nhớ clean lần cuối)
            paper.setTotalPage(pageCount);
            paper.setDoi(clean(doi));
            // arXiv có thể thêm trường riêng nếu bạn muốn
            paper.setTitle(clean(title));
            paper.setAuthorName(clean(author));
            paper.setPublicationYear(year != null ? String.valueOf(year) : null);
            paper.setJournalName(clean(journal)); // nếu Paper có field này

            doc.close();

            // 7) (tuỳ chọn) Enrich qua backend nếu có DOI
            if (paper.getDoi() != null) {
                enrichFromBackendByDoi(paper.getDoi());
            }

        } catch (Exception e) {
            Log.w("PDF_META", "extract meta failed, fallback to filename only", e);
            paper.setTotalPage(PdfMetadataUtil.extractMetadata(this, uploadedFile).pageCount);
            paper.setTitle(clean(stripExt(uploadedFile.getName())));
            paper.setAuthorName(null);
            paper.setPublicationYear(null);
            paper.setDoi(null);
        }

//        PdfMetadataUtil.PdfInfo info = PdfMetadataUtil.extractMetadata(this, uploadedFile);
//        paper.setTotalPage(info.pageCount);


    }

    private static String stripExt(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private void enrichFromBackendByDoi(String doi) {
        paperRepository.resolveDoi(doi, new Callback<PaperMetaDto>() {
            @Override public void onResponse(Call<PaperMetaDto> c, Response<PaperMetaDto> r) {
                if (r.isSuccessful() && r.body()!=null) {
                    PaperMetaDto m = r.body();
                    // chỉ overwrite nếu trường đang rỗng/thiếu
                    if (isEmpty(paper.getTitle())) paper.setTitle(clean(m.title));
                    if (isEmpty(paper.getAuthorName())) paper.setAuthorName(clean(joinAuthors(m.authors)));
                    if (isEmpty(paper.getPublicationYear()) && m.year != null) paper.setPublicationYear(String.valueOf(m.year));
                    if (isEmpty(paper.getJournalName())) paper.setJournalName(clean(m.venue));
                }
            }
            @Override public void onFailure(Call<PaperMetaDto> c, Throwable t) { /* bỏ qua */ }
        });
    }
    private boolean isEmpty(String s){ return s==null || s.trim().isEmpty(); }
    private String joinAuthors(List<AuthorDto> list){
        if (list==null || list.isEmpty()) return null;
        List<String> names = new ArrayList<>();
        for (AuthorDto a: list) names.add(a.fullName);
        return String.join(", ", names);
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
                    finish();
                } else {
                    Toast.makeText(ImportPaperActivity.this, "Thêm paper vào server thất bại", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<AddPaperResponse> call, Throwable t) {
                Toast.makeText(ImportPaperActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

}