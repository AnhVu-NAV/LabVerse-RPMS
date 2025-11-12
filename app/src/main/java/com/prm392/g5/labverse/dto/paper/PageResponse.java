package com.prm392.g5.labverse.dto.paper;

import java.util.List;

public class PageResponse<T> {
    public List<T> content;
    public int number;
    public int size;
    public long totalElements;
    public int totalPages;
    public boolean last;
}
