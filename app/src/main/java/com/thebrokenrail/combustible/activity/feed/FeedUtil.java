package com.thebrokenrail.combustible.activity.feed;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.elevation.ElevationOverlayProvider;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.comment.FlatCommentFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;

public class FeedUtil {
    public static void setupSwipeToRefresh(SwipeRefreshLayout swipeRefreshLayout, FeedAdapter<?> adapter) {
        // Connect Adapter To Layout
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh(false, () -> swipeRefreshLayout.setRefreshing(false)));

        // Theming
        Context context = swipeRefreshLayout.getContext();
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
        ElevationOverlayProvider elevationOverlayProvider = new ElevationOverlayProvider(context);
        @ColorInt int backgroundColor = ContextCompat.getColor(context, typedValue.resourceId);
        backgroundColor = elevationOverlayProvider.compositeOverlay(backgroundColor, context.getResources().getDimension(R.dimen.swipe_to_refresh_elevation));
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(backgroundColor);
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        swipeRefreshLayout.setColorSchemeResources(typedValue.resourceId);
    }

    private static void checkLoadingStatus(RecyclerView recyclerView, LinearLayoutManager layoutManager) {
        FeedAdapter<?> adapter = (FeedAdapter<?>) recyclerView.getAdapter();
        if (adapter != null) {
            recyclerView.post(() -> adapter.checkLoadingStatus(layoutManager));
        }
    }

    public static void setupRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext()) {
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                checkLoadingStatus(recyclerView, this);
            }
        };
        recyclerView.setLayoutManager(layoutManager);

        // Track Scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                checkLoadingStatus(recyclerView, layoutManager);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Pause Image Loading While Scrolling Comments
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(recyclerView.getContext()).resumeRequests();
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                    if (adapter instanceof FlatCommentFeedAdapter) {
                        Glide.with(recyclerView.getContext()).pauseRequests();
                    }
                }
            }
        });
    }

    public static void setupPrerequisites(RecyclerView recyclerView, FeedPrerequisites prerequisites) {
        prerequisites.listen(prerequisite -> {
            if (prerequisite == FeedPrerequisites.COMPLETED) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    checkLoadingStatus(recyclerView, layoutManager);
                }
            }
        });
    }
}
