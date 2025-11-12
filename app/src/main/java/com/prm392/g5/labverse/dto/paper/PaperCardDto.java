package com.prm392.g5.labverse.dto.paper;

public class PaperCardDto {
    public String id;
    public String title;
    public String authors;
    public String journal;
    public String status;   // UNREAD/READING/FINISHED
    public int progress;    // %
    public boolean favorite;
    public String createdAt;
    public String lastReadAt;
}
