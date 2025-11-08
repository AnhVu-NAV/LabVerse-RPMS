package com.prm392.g5.labverse.dto.readingStatus;

public class ReadingStatusRequest{
        private String id;
        private String userId;
        private String paperId;
        private int currentPage;

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public ReadingStatusRequest() {
    }

    public ReadingStatusRequest(String id, String userId, String paperId, int currentPage) {
        this.id = id;
        this.userId = userId;
        this.paperId = paperId;
        this.currentPage = currentPage;
    }
}
