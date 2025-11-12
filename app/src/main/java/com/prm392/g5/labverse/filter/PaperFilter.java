package com.prm392.g5.labverse.filter;

public class PaperFilter {
    public final String author;
    public final String journal;
    public final String tag;
    public final String year;

    public PaperFilter(String author, String journal, String tag, String year) {
        this.author = author == null ? "" : author;
        this.journal = journal == null ? "" : journal;
        this.tag = tag == null ? "" : tag;
        this.year = year == null ? "" : year;
    }
}
