package com.prm392.g5.labverse.dto.paper;

import java.time.LocalDateTime;

public class PaperSummaryDTO {
    public String id;
    public String title;
    public String authorName;
    public String journalName;
    public int totalPage;
    public Integer currentPage;
    public String status;
    public String createdAt;  // Android thường để String ISO 8601
    public String updatedAt;
}
