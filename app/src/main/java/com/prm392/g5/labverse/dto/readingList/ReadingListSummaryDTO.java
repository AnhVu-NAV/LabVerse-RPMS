package com.prm392.g5.labverse.dto.readingList;

public class ReadingListSummaryDTO {
    public String id;
    public String name;
    public String description;    // nullable
    public int paperCount;
    public String createdAt;      // ISO string từ BE, ví dụ "2025-11-09T14:50:01.319372"

    public ReadingListSummaryDTO() {

    }

    public ReadingListSummaryDTO(String id, String name, String description, int paperCount, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.paperCount = paperCount;
        this.createdAt = createdAt;
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

    public int getPaperCount() {
        return paperCount;
    }

    public void setPaperCount(int paperCount) {
        this.paperCount = paperCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}