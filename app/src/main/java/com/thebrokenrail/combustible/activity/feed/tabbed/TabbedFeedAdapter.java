package com.thebrokenrail.combustible.activity.feed.tabbed;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.FeedUtil;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;

import java.util.List;
import java.util.Map;

class TabbedFeedAdapter extends RecyclerView.Adapter<TabbedFeedAdapter.TabViewHolder> {
    static class TabViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recyclerView;

        private TabViewHolder(@NonNull View itemView, RecyclerView recyclerView) {
            super(itemView);
            this.recyclerView = recyclerView;
        }
    }

    private final FeedPrerequisites prerequisites;
    private final List<Map.Entry<Integer, FeedAdapter<?>>> tabs;

    TabbedFeedAdapter(FeedPrerequisites prerequisites, List<Map.Entry<Integer, FeedAdapter<?>>> tabs) {
        this.prerequisites = prerequisites;
        this.tabs = tabs;
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        // Create SwipeRefreshLayout
        SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(context);
        swipeRefreshLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));

        // Create RecyclerView
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new SwipeRefreshLayout.LayoutParams(SwipeRefreshLayout.LayoutParams.MATCH_PARENT, SwipeRefreshLayout.LayoutParams.MATCH_PARENT));
        recyclerView.setClipToPadding(false);
        FeedUtil.setupRecyclerView(recyclerView);
        FeedUtil.setupPrerequisites(recyclerView, prerequisites);
        swipeRefreshLayout.addView(recyclerView);

        // Edge-To-Edge
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            recyclerView.setPadding(insets.left, 0, insets.right, insets.bottom + context.getResources().getDimensionPixelSize(R.dimen.feed_item_margin));
            return windowInsets;
        });
        recyclerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.requestApplyInsets();
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
            }
        });

        // Return
        return new TabViewHolder(swipeRefreshLayout, recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        FeedAdapter<?> adapter = tabs.get(position).getValue();
        holder.recyclerView.setAdapter(adapter);
        FeedUtil.setupSwipeToRefresh((SwipeRefreshLayout) holder.itemView, adapter);
        holder.recyclerView.setTag("tab-" + position);
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }
}
