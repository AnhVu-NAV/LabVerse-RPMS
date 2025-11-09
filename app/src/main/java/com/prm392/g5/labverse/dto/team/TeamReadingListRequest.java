package com.prm392.g5.labverse.dto.team;

public class TeamReadingListRequest {
    private String name;
    private String description;

    public TeamReadingListRequest() {
    }

    public TeamReadingListRequest(String name, String description) {
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