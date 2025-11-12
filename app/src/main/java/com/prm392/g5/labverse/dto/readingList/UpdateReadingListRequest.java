package com.prm392.g5.labverse.dto.readingList;

public class UpdateReadingListRequest {
    public String name;
    public String description;

    public UpdateReadingListRequest() {
    }

    public UpdateReadingListRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}