package com.prm392.g5.labverse.dto.team;

public class TeamReadingListPaperResponse {
    private String id;
    private String readingListId;
    private String paperId;
    private String priority;
    private String title;
    private String authorName;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReadingListId() { return readingListId; }
    public void setReadingListId(String readingListId) { this.readingListId = readingListId; }

    public String getPaperId() { return paperId; }
    public void setPaperId(String paperId) { this.paperId = paperId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}
