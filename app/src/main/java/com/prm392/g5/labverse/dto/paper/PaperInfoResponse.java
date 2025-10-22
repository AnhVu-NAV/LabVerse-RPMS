package com.prm392.g5.labverse.dto.paper;

public class PaperInfoResponse{

    private String id;
    private String s3Key;
    private int totalPage;
    private int currentPage;

    private String authorName;
    private String title;
    private String publicationYear;
    private String doi;
    //todo neu sau bo sung them truong nao thi them vao


    public String getId() {
        return id;
    }

    public String getS3Key() {
        return s3Key;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public String getDoi() {
        return doi;
    }
}
