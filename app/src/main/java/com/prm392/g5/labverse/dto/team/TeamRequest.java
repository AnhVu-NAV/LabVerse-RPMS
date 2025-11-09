package com.prm392.g5.labverse.dto.team;

public class TeamRequest {
    private String id;
    private String name;
    private String description;

    public TeamRequest() {
    }

    public TeamRequest(String id, String description, String name) {
        this.id = id;
        this.description = description;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
