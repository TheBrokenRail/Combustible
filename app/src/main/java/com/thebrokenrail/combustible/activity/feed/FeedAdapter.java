package com.thebrokenrail.combustible.activity.feed;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link RecyclerView} adapter for seamless scrolling of multi-page feeds.
 * @param <T> Type of element in list
 */
public abstract class FeedAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * Represents the possible values of {@link RecyclerView.ViewHolder#getItemViewType()} in this adapter.
     */
    protected enum ViewType {
        HEADER,
        ELEMENT,
        NEXT_PAGE
    }

    private enum LoadingStatus {
        PENDING,
        LOADING,
        ERROR,
        DONE
    }
    private LoadingStatus loadingStatus = LoadingStatus.PENDING;

    /**
     * Elements in adapter.
     */
    protected final List<T> dataset = new ArrayList<>();

    private static final int FIRST_PAGE = 1; // This Is 1-Indexed
    private int nextPage = FIRST_PAGE;
    private final LinearLayoutManager layoutManager;
    private int refreshVersion = 0;
    private final RecyclerView parent;

    /**
     * The connection to Lemmy.
     */
    protected final Connection connection;

    /**
     * Creates a FeedAdapter and attaches it.
     * @param parent The {@link RecyclerView} this adapter will be attached to
     * @param connection The connection to Lemmy
     */
    public FeedAdapter(RecyclerView parent, Connection connection) {
        this.parent = parent;
        layoutManager = new LinearLayoutManager(parent.getContext()) {
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                parent.post(() -> checkLoadingStatus());
            }
        };
        parent.setLayoutManager(layoutManager);
        parent.setAdapter(this);

        // Track Scrolling
        parent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                parent.post(() -> checkLoadingStatus());
            }
        });

        // Start Load
        this.connection = connection;
        load();
    }

    /**
     * Checks if this adapter has a header.
     * @return True if it has a header, false otherwise
     */
    protected abstract boolean hasHeader();

    /**
     * Create the adapter's header (if it exists).
     * @param parent The header's parent {@link ViewGroup}
     * @return The new header
     */
    protected abstract View createHeader(ViewGroup parent);

    /**
     * Creates the {@link RecyclerView.ViewHolder} for an element
     * @param parent The header's parent {@link ViewGroup}
     * @return The new {@link RecyclerView.ViewHolder}
     */
    protected abstract RecyclerView.ViewHolder createItem(ViewGroup parent);

    @NonNull
    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ViewType.HEADER.ordinal()) {
            return new RecyclerView.ViewHolder(createHeader(parent)) {};
        } else if (viewType == ViewType.ELEMENT.ordinal()) {
            return createItem(parent);
        } else {
            NextPageLoader loader = new NextPageLoader(parent, this);
            return loader.viewHolder;
        }
    }

    /**
     * Bind an element's information to its {@link RecyclerView.ViewHolder}
     * @param holder The target {@link RecyclerView.ViewHolder}
     * @param position The element to be bound
     */
    protected abstract void bindElement(@NonNull RecyclerView.ViewHolder holder, int position);

    /**
     * Bind the header's information to its {@link View}
     * @param root The target {@link View}
     */
    protected abstract void bindHeader(View root);

    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ViewType.ELEMENT.ordinal()) {
            if (hasHeader()) {
                position--;
            }
            bindElement(holder, position);
        } else if (holder.getItemViewType() == ViewType.NEXT_PAGE.ordinal()) {
            NextPageLoader nextPageLoader = (NextPageLoader) holder.itemView;
            if (loadingStatus == LoadingStatus.ERROR) {
                nextPageLoader.setupError();
            } else {
                nextPageLoader.setupProgress();
            }
        } else if (holder.getItemViewType() == ViewType.HEADER.ordinal()) {
            bindHeader(holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        // Items In List
        int count = dataset.size();
        // Next Page Loader
        if (loadingStatus != LoadingStatus.DONE) {
            count++;
        }
        // Header
        if (hasHeader()) {
            count++;
        }
        // Return
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && hasHeader()) {
            return ViewType.HEADER.ordinal();
        } else if (position == getNextPageLoaderPosition()) {
            return ViewType.NEXT_PAGE.ordinal();
        } else {
            return ViewType.ELEMENT.ordinal();
        }
    }

    private int getNextPageLoaderPosition() {
        int position = dataset.size();
        if (hasHeader()) {
            position++;
        }
        return position;
    }

    private void updateLoadingStatus(LoadingStatus loadingStatus) {
        // Set Status
        LoadingStatus oldLoadingStatus = this.loadingStatus;
        this.loadingStatus = loadingStatus;

        // Check If Status Has Changed
        if (loadingStatus != oldLoadingStatus) {
            int position = getNextPageLoaderPosition();
            if (loadingStatus == LoadingStatus.DONE) {
                // Remove Element
                notifyItemRemoved(position);
            } else if (oldLoadingStatus == LoadingStatus.DONE) {
                // Create Element
                notifyItemInserted(position);
            } else {
                // Update Element
                notifyItemChanged(position);
            }
        }
    }

    /**
     * Load the specified page of elements.
     * @param page The page to load
     * @param successCallback Callback that is executed on success
     * @param errorCallback Callback that is executed on failure
     */
    protected abstract void loadPage(int page, Consumer<List<T>> successCallback, Runnable errorCallback);

    /**
     * Return the position of the first element if the adapter.
     * @return The element's position
     */
    protected int getFirstElementPosition() {
        if (hasHeader()) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Add the provided elements to this adapter.
     * @param elements The new elements
     * @param manualNotifications True if this method is responsible for notifying the {@link RecyclerView}, false otherwise
     */
    protected void addElements(List<T> elements, boolean manualNotifications) {
        int insertPosition = getFirstElementPosition() + dataset.size();
        dataset.addAll(elements);
        if (manualNotifications) {
            notifyItemRangeInserted(insertPosition, elements.size());
        }
    }

    private LoadingStatus addPage(List<T> elements, boolean manualNotifications) {
        nextPage++;
        int size = elements.size();
        addElements(elements, manualNotifications);
        if (size < Util.ELEMENTS_PER_PAGE) {
            // No More Elements To Load
            return LoadingStatus.DONE;
        } else {
            // More Elements Available
            return LoadingStatus.PENDING;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    void load() {
        // Check Status
        if (loadingStatus != LoadingStatus.PENDING && loadingStatus != LoadingStatus.ERROR) {
            return;
        }
        updateLoadingStatus(LoadingStatus.LOADING);

        // Load Page
        int oldRefreshVersion = refreshVersion;
        loadPage(nextPage, data -> {
            if (refreshVersion == oldRefreshVersion) {
                LoadingStatus newLoadingStatus = addPage(data, true);
                updateLoadingStatus(newLoadingStatus);
            }
        }, () -> {
            if (refreshVersion == oldRefreshVersion) {
                updateLoadingStatus(LoadingStatus.ERROR);
            }
        });
    }

    private void checkLoadingStatus() {
        if (getItemViewType(layoutManager.findLastVisibleItemPosition()) == ViewType.NEXT_PAGE.ordinal() && loadingStatus == LoadingStatus.PENDING) {
            // Trigger Page Load
            load();
        }
    }

    /**
     * Remove all elements from adapter.
     */
    protected void clear() {
        dataset.clear();
    }

    /**
     * Refresh the adapter.
     * @param hard True if currently loaded content should disappear during the refresh, false otherwise
     * @param callback Callback that is executed on completion
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refresh(boolean hard, Runnable callback) {
        // Disable Previous Callbacks
        refreshVersion++;

        // Different Refresh Types
        if (hard) {
            // Hard Refresh
            nextPage = FIRST_PAGE;
            clear();
            loadingStatus = LoadingStatus.PENDING;
            notifyDataSetChanged();
            callback.run();
        } else {
            // Soft Refresh

            // Disable Next Page Loader
            updateLoadingStatus(LoadingStatus.DONE);

            // Refresh
            int oldRefreshVersion = refreshVersion;
            parent.post(() -> {
                nextPage = FIRST_PAGE;
                loadPage(nextPage, data -> {
                    if (oldRefreshVersion == refreshVersion) {
                        clear();
                        loadingStatus = addPage(data, false);
                        notifyDataSetChanged();
                        callback.run();
                    }
                }, () -> {
                    if (oldRefreshVersion == refreshVersion) {
                        clear();
                        loadingStatus = LoadingStatus.ERROR;
                        notifyDataSetChanged();
                        callback.run();
                    }
                });
            });
        }
    }
}
