package com.prm392.g5.labverse.dto.team;

public class TeamReadingStatusResponse {
    private String id;
    private String userId;
    private String userName;
    private String userEmail;
    private String paperId;
    private int currentPage;
    private int totalPage;  // Thêm trường totalPage

    public TeamReadingStatusResponse(String id, String userId, String userName, String userEmail, String paperId, int currentPage, int totalPage) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.paperId = paperId;
        this.currentPage = currentPage;
        this.totalPage = totalPage;  // Gán totalPage
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
}
