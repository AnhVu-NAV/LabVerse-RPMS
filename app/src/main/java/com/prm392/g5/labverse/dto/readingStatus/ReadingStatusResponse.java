package com.prm392.g5.labverse.dto.readingStatus;

public class ReadingStatusResponse {
        private String id;
        private String userId;
        private String paperId;
        private int currentPage;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPaperId() {
        return paperId;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public ReadingStatusResponse(String id, String userId, String paperId, int currentPage) {
        this.id = id;
        this.userId = userId;
        this.paperId = paperId;
        this.currentPage = currentPage;
    }
}
