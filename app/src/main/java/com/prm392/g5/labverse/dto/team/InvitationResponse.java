package com.prm392.g5.labverse.dto.team;

public class InvitationResponse {
    private String id;
    private String teamId;
    private String teamName;
    private String teamDescription;
    private String invitedBy;
    private String invitedUserEmail;
    private String status;
    private String invitedAt;
    private String expiresAt;
    private int daysLeft;
    private boolean expired;

    public InvitationResponse() {
    }

    public InvitationResponse(String id, String teamId, String teamName, String teamDescription,
                              String invitedBy, String invitedUserEmail, String status,
                              String invitedAt, String expiresAt, int daysLeft, boolean expired) {
        this.id = id;
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamDescription = teamDescription;
        this.invitedBy = invitedBy;
        this.invitedUserEmail = invitedUserEmail;
        this.status = status;
        this.invitedAt = invitedAt;
        this.expiresAt = expiresAt;
        this.daysLeft = daysLeft;
        this.expired = expired;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamDescription() {
        return teamDescription;
    }

    public void setTeamDescription(String teamDescription) {
        this.teamDescription = teamDescription;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getInvitedUserEmail() {
        return invitedUserEmail;
    }

    public void setInvitedUserEmail(String invitedUserEmail) {
        this.invitedUserEmail = invitedUserEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(String invitedAt) {
        this.invitedAt = invitedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    @Override
    public String toString() {
        return "InvitationResponse{" +
                "id='" + id + '\'' +
                ", teamId='" + teamId + '\'' +
                ", teamName='" + teamName + '\'' +
                ", teamDescription='" + teamDescription + '\'' +
                ", invitedBy='" + invitedBy + '\'' +
                ", invitedUserEmail='" + invitedUserEmail + '\'' +
                ", status='" + status + '\'' +
                ", invitedAt='" + invitedAt + '\'' +
                ", expiresAt='" + expiresAt + '\'' +
                ", daysLeft=" + daysLeft +
                ", expired=" + expired +
                '}';
    }
}