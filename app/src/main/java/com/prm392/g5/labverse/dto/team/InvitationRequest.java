package com.prm392.g5.labverse.dto.team;

public class InvitationRequest {
    private String teamId;
    private String email;

    public InvitationRequest() {
    }

    public InvitationRequest(String teamId, String email) {
        this.teamId = teamId;
        this.email = email;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "InvitationRequest{" +
                "teamId='" + teamId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}