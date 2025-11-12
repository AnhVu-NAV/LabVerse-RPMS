package com.prm392.g5.labverse.dto.paper;

import java.util.List;

public class AddPapersRequest {
    private List<String> paperIds;

    public AddPapersRequest(List<String> paperIds) {
        this.paperIds = paperIds;
    }

    public List<String> getPaperIds() {
        return paperIds;
    }

    public void setPaperIds(List<String> paperIds) {
        this.paperIds = paperIds;
    }
}
