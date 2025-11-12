package com.prm392.g5.labverse.util;

import com.prm392.g5.labverse.dto.paper.PaperSummaryDTO;
import com.prm392.g5.labverse.entity.Paper;

import java.util.ArrayList;
import java.util.List;

public final class MapperUtils {
    private MapperUtils() {}

    public static List<Paper> mapDtoToPaper(List<PaperSummaryDTO> dtos) {
        List<Paper> out = new ArrayList<>();
        if (dtos == null) return out;
        for (PaperSummaryDTO d : dtos) {
            Paper p = new Paper();
            p.setId(d.id);
            p.setTitle(d.title);
            p.setAuthorName(d.authorName);
            p.setJournalName(d.journalName);
            p.setTotalPage(d.totalPage);
            // Nếu muốn hiển thị progress, thêm field vào DTO rồi set ở đây.
            out.add(p);
        }
        return out;
    }
}
