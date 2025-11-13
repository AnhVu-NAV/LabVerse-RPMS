package com.prm392.g5.labverse.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.prm392.g5.labverse.fragment.AnnotationsFragment;
import com.prm392.g5.labverse.fragment.CitationFragment;
import com.prm392.g5.labverse.fragment.DiscussionFragment;
import com.prm392.g5.labverse.fragment.ReadFragment;

public class PaperDetailPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 4;

    private final String paperId;

    public PaperDetailPagerAdapter(@NonNull FragmentActivity fa, @NonNull String paperId) {
        super(fa);
        this.paperId = paperId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ReadFragment.newInstance(paperId);
            case 1:
                return CitationFragment.newInstance();
            case 2:
                return AnnotationsFragment.newInstance();
            case 3:
                return DiscussionFragment.newInstance();
            default:
                return ReadFragment.newInstance(paperId);
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}

