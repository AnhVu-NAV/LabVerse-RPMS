package com.prm392.g5.labverse.dto.readingList;

public class CreateReadingListRequest {
    public String name;
    public String description;

    public CreateReadingListRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
