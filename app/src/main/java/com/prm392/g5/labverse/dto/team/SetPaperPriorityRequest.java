package com.prm392.g5.labverse.dto.team;

public class SetPaperPriorityRequest {
    private String priority;

    public SetPaperPriorityRequest(String priority) {
        this.priority = priority;
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}