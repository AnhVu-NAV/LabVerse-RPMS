package com.prm392.g5.labverse.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prm392.g5.labverse.config.AppDatabase;
import com.prm392.g5.labverse.dao.PaperDao;
import com.prm392.g5.labverse.dao.ReadingListDao;
import com.prm392.g5.labverse.entity.Paper;
import com.prm392.g5.labverse.entity.ReadingList;
import com.prm392.g5.labverse.filter.PaperFilter;
import com.prm392.g5.labverse.search.SearchItem;
import com.prm392.g5.labverse.util.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends AndroidViewModel {

    private final PaperDao paperDao;
    private final ReadingListDao readingListDao;

    private final MutableLiveData<List<SearchItem>> results = new MutableLiveData<>(List.of());
    public LiveData<List<SearchItem>> getResults() { return results; }

    public SearchViewModel(@NonNull Application app) {
        super(app);
        AppDatabase db = AppDatabase.getInstance(app);
        paperDao = db.paperDao();
        readingListDao = db.readingListDao();
    }

    public void search(String keyword, PaperFilter f) {
        final String kwLike = likeWrap(keyword);
        final String author = likeWrap(f.author);
        final String journal = likeWrap(f.journal);
        final String tag = likeWrap(f.tag);
        final String year = likeWrap(f.year);

        AppExecutors.io().execute(() -> {
            // 1) Papers
            List<Paper> papers = paperDao.searchAdvanced(
                    kwLike, author, journal, tag, year
            );

            // 2) Reading lists (match theo name)
            List<ReadingList> lists = readingListDao.searchByName(kwLike);

            // 3) Merge
            List<SearchItem> merged = new ArrayList<>(lists.size() + papers.size());
            for (ReadingList rl : lists) merged.add(SearchItem.fromReadingList(rl));
            for (Paper p : papers) merged.add(SearchItem.fromPaper(p));

            // DEBUG
            android.util.Log.d(
                    "SearchVM",
                    "search items=" + merged.size()
                            + " kw=" + kwLike + " author=" + author + " journal=" + journal + " year=" + year
            );
            results.postValue(merged);
        });
    }

    private String likeWrap(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        // an toàn: escape ký tự wildcard
        t = t.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + t + "%";
    }
}
