package com.prm392.g5.labverse.dto.readingList;

public class ReadingListResponse {
    public String id;           // UUID từ BE
    public String name;
    public String description;
    public String createdAt;    // ISO string (vd: "2025-11-10T03:20:15.123")
    public Integer paperCount;  // có thể null nếu BE chưa set

    // có thể thêm updatedAt, deleteFlag... nếu BE trả
}
