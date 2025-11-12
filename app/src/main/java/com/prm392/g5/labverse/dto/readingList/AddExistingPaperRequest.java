package com.prm392.g5.labverse.dto.readingList;


public class AddExistingPaperRequest {
    public String paperId;
    public Integer position;

    public AddExistingPaperRequest() {
    }

    public AddExistingPaperRequest(String paperId, Integer position) {
        this.paperId = paperId;
        this.position = position;
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}