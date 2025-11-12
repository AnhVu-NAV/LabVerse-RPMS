package com.prm392.g5.labverse.dto.readingList;

import com.google.gson.annotations.SerializedName;

public class ReadingListItemDTO {
    @SerializedName("id")
    private Long id;

    @SerializedName("paperId")
    private String paperId;

    @SerializedName("position")
    private Integer position;

    public ReadingListItemDTO() {}

    public Long getId() { return id; }
    public String getPaperId() { return paperId; }
    public Integer getPosition() { return position; }

    public void setId(Long id) { this.id = id; }
    public void setPaperId(String paperId) { this.paperId = paperId; }
    public void setPosition(Integer position) { this.position = position; }
}
