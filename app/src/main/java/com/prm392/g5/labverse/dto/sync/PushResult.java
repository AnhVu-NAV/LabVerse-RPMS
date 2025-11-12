package com.prm392.g5.labverse.dto.sync;

import com.prm392.g5.labverse.entity.PaperAnnotation;

import java.util.ArrayList;
import java.util.List;

/** Kết quả sau khi push annotations: BE trả về các annotation đã nhận/ghi */
public class PushResult {
    public List<Accepted> accepted = new ArrayList<>();

    public List<PaperAnnotation> toCleanEntities(){
        List<PaperAnnotation> out = new ArrayList<>();
        for (Accepted a : accepted){
            PaperAnnotation e = new PaperAnnotation();
            e.id = a.id;
            e.paperId = a.paperId;
            e.syncState = "CLEAN";
            e.updatedAtEpoch = a.updatedAtEpoch;
            out.add(e);
        }
        return out;
    }

    public static class Accepted {
        public String id, paperId, type, payload;
        public int page;
        public long updatedAtEpoch;
    }
}
