package com.prm392.g5.labverse.search;

import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.ReadingList;

public class SearchItem {
    public enum Type { PAPER, READING_LIST }
    public final Type type;
    public final Paper paper;
    public final ReadingList readingList;

    private SearchItem(Type type, Paper paper, ReadingList readingList) {
        this.type = type;
        this.paper = paper;
        this.readingList = readingList;
    }

    public static SearchItem fromPaper(Paper p) {
        return new SearchItem(Type.PAPER, p, null);
    }

    public static SearchItem fromReadingList(ReadingList rl) {
        return new SearchItem(Type.READING_LIST, null, rl);
    }
}
