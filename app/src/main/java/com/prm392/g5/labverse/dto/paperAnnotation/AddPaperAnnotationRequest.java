package com.prm392.g5.labverse.dto.paperAnnotation;

public class AddPaperAnnotationRequest {
    private String id;
    private String userId;
    private String paperId;
    private String annotationS3Key;
    private String updateAt;

    public AddPaperAnnotationRequest() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public void setAnnotationS3Key(String annotationS3Key) {
        this.annotationS3Key = annotationS3Key;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }
}
