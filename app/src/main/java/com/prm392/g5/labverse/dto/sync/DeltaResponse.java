package com.prm392.g5.labverse.dto.sync;

import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.PaperAnnotation;
import com.prm392.g5.labverse.entity.ReadingList;

import java.util.ArrayList;
import java.util.List;

/** Phản hồi delta tối thiểu từ BE */
public class DeltaResponse {
    public List<PaperDto> papers = new ArrayList<>();
    public List<ReadingListDto> lists = new ArrayList<>();
    public List<AnnotationDto> annotations = new ArrayList<>();

    // ---- MAPPER cho Room entities ----
    public List<Paper> papersMappedForRoom(String userId){
        List<Paper> out = new ArrayList<>();
        for (PaperDto d : papers){
            Paper p = new Paper();
            p.setId(d.id);
            p.setTitle(d.title);
            p.setAuthorName(d.authors);
            p.setJournalName(d.journal);
            p.setPublicationYear(d.pubYear);
            // nếu có user-scope, gán userId vào Paper nếu schema có cột; nếu không, bỏ
            out.add(p);
        }
        return out;
    }

    public List<ReadingList> listsMappedForRoom(String userId){
        List<ReadingList> out = new ArrayList<>();
        for (ReadingListDto d : lists){
            ReadingList rl = new ReadingList();
            rl.setId(d.id);
            rl.setName(d.name);
            rl.setOwnerUserId(userId);
            out.add(rl);
        }
        return out;
    }

    public List<PaperAnnotation> annotationsMappedForRoom(String userId){
        List<PaperAnnotation> out = new ArrayList<>();
        for (AnnotationDto a : annotations){
            PaperAnnotation e = new PaperAnnotation();
            e.id = a.id;
            e.userId = userId;
            e.paperId = a.paperId;
            e.setSyncState("CLEAN");
            e.updatedAtEpoch = a.updatedAtEpoch;
            out.add(e);
        }
        return out;
    }

    // ---- DTO con (điền tối thiểu) ----
    public static class PaperDto { public String id, title, authors, journal, pubYear; }
    public static class ReadingListDto { public String id, name; }
    public static class AnnotationDto {
        public String id, paperId, type, payload;
        public int page;
        public long updatedAtEpoch;
    }
}
