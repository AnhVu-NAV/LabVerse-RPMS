package com.prm392.g5.labverse.dto.team;

public class TeamReadingListResponse {
    private String id;
    private String teamId;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
    public TeamReadingListResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamId() { return teamId; }

    public void setTeamId(String teamId) { this.teamId = teamId; }

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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}