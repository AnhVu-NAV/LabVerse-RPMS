package com.prm392.g5.labverse.util;

import android.content.Context;
import android.util.Log;


import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.IOException;

public class PdfMetadataUtil {

    public static PdfInfo extractMetadata(Context context, File pdfFile) {
        // Ensure PDFBox is initialized
        PDFBoxResourceLoader.init(context);

        PdfInfo info = new PdfInfo();
        PDDocument document = null;

        try {
            document = PDDocument.load(pdfFile);

            // Page count
            info.pageCount = document.getNumberOfPages();

            // Metadata info
            PDDocumentInformation metadata = document.getDocumentInformation();
            if (metadata != null) {
                info.title = metadata.getTitle();
                info.author = metadata.getAuthor();
                info.subject = metadata.getSubject();
                info.keywords = metadata.getKeywords();
                info.creator = metadata.getCreator();
                info.producer = metadata.getProducer();
            }


        } catch (IOException e) {
            Log.e("PdfMetadataUtil", "Error reading PDF metadata", e);
        } finally {
            try {
                if (document != null) document.close();
            } catch (IOException ignore) {}
        }

        return info;
    }

    public static class PdfInfo {
        /** cứ viết hết những gì có thể lấy ra chứ cần gì thí giữ lại nhé TA */
        public int pageCount;
        public String title;
        public String author;
        public String subject;
        public String keywords;
        public String creator;
        public String producer;

        @Override
        public String toString() {
            return "pages=" + pageCount +
                    ", title=" + title +
                    ", author=" + author ;
        }
    }
}
