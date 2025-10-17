package com.prm392.g5.labverse.dto.paper;

public class AddPaperRequest {

    private String s3Key;
    private int totalPage;

    private String authorName;
    private String publicationYear;
    private String title;
    private String doi;

    public AddPaperRequest() {
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }
}
