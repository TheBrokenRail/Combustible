package com.thebrokenrail.combustible.activity.feed.tabbed;

import android.content.Context;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.thebrokenrail.combustible.activity.feed.util.adapter.base.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.util.FeedUtil;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.util.EdgeToEdge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class TabbedFeedAdapter extends RecyclerView.Adapter<TabbedFeedAdapter.TabViewHolder> {
    static class TabViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recyclerView;

        private TabViewHolder(@NonNull View itemView, RecyclerView recyclerView) {
            super(itemView);
            this.recyclerView = recyclerView;
        }
    }

    public static class ScrollState extends ViewModel {
        private final Map<Integer, Parcelable> state = new HashMap<>();

        private void save(int id, RecyclerView recyclerView) {
            state.put(id, Objects.requireNonNull(recyclerView.getLayoutManager()).onSaveInstanceState());
        }

        private void load(int id, RecyclerView recyclerView) {
            if (state.containsKey(id)) {
                Objects.requireNonNull(recyclerView.getLayoutManager()).onRestoreInstanceState(state.get(id));
            }
        }
    }

    private final ScrollState scrollState;
    private final FeedPrerequisites prerequisites;
    private final List<Map.Entry<Integer, FeedAdapter<?>>> tabs;

    TabbedFeedAdapter(ViewModelProvider viewModelProvider, FeedPrerequisites prerequisites, List<Map.Entry<Integer, FeedAdapter<?>>> tabs) {
        scrollState = viewModelProvider.get(ScrollState.class);
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
        EdgeToEdge.setupScroll(recyclerView);
        recyclerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.requestApplyInsets();
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
            }
        });

        // Scroll State
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Get ID
                int id = -1;
                RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter != null) {
                    for (int i = 0; i < tabs.size(); i++) {
                        Map.Entry<Integer, FeedAdapter<?>> tab = tabs.get(i);
                        if (tab.getValue() == adapter) {
                            id = i;
                            break;
                        }
                    }
                }
                // Store
                if (id != -1) {
                    scrollState.save(id, recyclerView);
                }
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

        // Scroll State
        scrollState.load(position, holder.recyclerView);
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }
}
