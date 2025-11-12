package com.prm392.g5.labverse.entity;

public class ReadingListItem {
    private String id;
    private int position;
    private Paper paper;  // quan trọng nhất: để lấy paper xem UI

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public Paper getPaper() { return paper; }
    public void setPaper(Paper paper) { this.paper = paper; }
}
