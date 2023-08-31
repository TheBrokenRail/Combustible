package com.thebrokenrail.combustible.activity.feed;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.dataset.SimpleFeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.util.ExtendableViewModel;
import com.thebrokenrail.combustible.util.Permissions;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.widget.NextPageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * {@link RecyclerView} adapter for seamless scrolling of multi-page feeds.
 * @param <T> Type of element in list
 */
public abstract class FeedAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private enum ViewType {
        HEADER,
        ELEMENT,
        NEXT_PAGE
    }

    private enum LoadingStatus {
        // Pending user interaction or prerequisites
        PENDING(NextPageLoader.DisplayMode.PROGRESS),
        // Currently loading data
        LOADING(NextPageLoader.DisplayMode.PROGRESS),
        // Failure loading data
        ERROR(NextPageLoader.DisplayMode.ERROR),
        // No more data to load
        DONE(NextPageLoader.DisplayMode.NONE);

        private final NextPageLoader.DisplayMode displayMode;
        LoadingStatus(NextPageLoader.DisplayMode displayMode) {
            this.displayMode = displayMode;
        }
    }

    /**
     * Class containing feed data that should be persisted after a configuration change.
     */
    protected static class ViewModel<T> {
        public ViewModel() {}

        /**
         * Elements in adapter.
         */
        public FeedDataset<T> dataset = null;

        private static final int FIRST_PAGE = 1; // This Is 1-Indexed
        private int nextPage = FIRST_PAGE;

        private LoadingStatus loadingStatus = LoadingStatus.PENDING;

        private void prepare() {
            // Requests are cancelled on configuration changes.
            if (loadingStatus == LoadingStatus.LOADING) {
                loadingStatus = LoadingStatus.PENDING;
            }
        }

        private void clear(FeedAdapter<T> adapter) {
            // Disable Previous Callbacks
            adapter.feedVersion++;
            // Clear Dataset
            dataset.clear(adapter.notifier);
            // Reset Next Page
            nextPage = ViewModel.FIRST_PAGE;
            // Reset Loading Status
            adapter.updateLoadingStatus(LoadingStatus.PENDING);
        }
    }

    /**
     * The view model.
     */
    protected final ExtendableViewModel rootViewModel;

    /**
     * Feed data that should be persisted after a configuration change.
     */
    protected final ViewModel<T> viewModel;

    /**
     * The connection to Lemmy.
     */
    protected final Connection connection;

    /**
     * General instance information.
     */
    protected GetSiteResponse site = null;

    /**
     * A parent view.
     */
    protected final View parent;

    /**
     * Permission manager.
     */
    protected final Permissions permissions = new Permissions();

    /**
     * The bridge between {@link FeedDataset} and {@link RecyclerView.Adapter}.
     */
    protected final FeedDataset.Notifier notifier = new FeedDataset.Notifier() {
        @Override
        public void insert(int position, int amount) {
            notifyItemRangeInserted(getFirstElementPosition() + position, amount);
        }

        @Override
        public void remove(int position, int amount) {
            notifyItemRangeRemoved(getFirstElementPosition() + position, amount);
        }

        @Override
        public void change(int position) {
            notifyItemChanged(getFirstElementPosition() + position);
        }
    };

    private int feedVersion = 0;
    private FeedPrerequisites prerequisites = null;

    // https://github.com/airbnb/epoxy/issues/224#issuecomment-305991898
    private static class ScrollingBugWorkaround extends RecyclerView.AdapterDataObserver {
        private final List<RecyclerView> recyclerViews = new ArrayList<>();

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            if (positionStart == 0) {
                for (RecyclerView recyclerView : recyclerViews) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    assert layoutManager != null;
                    if (positionStart == ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition()) {
                        layoutManager.scrollToPosition(0);
                    }
                }
            }
        }
    }
    private final ScrollingBugWorkaround scrollingBugWorkaround = new ScrollingBugWorkaround();

    /**
     * Create a FeedAdapter.
     * @param parent A parent {@link View} of this adapter
     * @param connection The connection to Lemmy
     * @param viewModelProvider View model provider
     */
    public FeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        this.connection = connection;

        // View Model
        rootViewModel = viewModelProvider.get(getClass().toString(), ExtendableViewModel.class);
        //noinspection unchecked
        viewModel = rootViewModel.get(ViewModel.class);
        viewModel.prepare();
        // Dataset
        if (viewModel.dataset == null) {
            viewModel.dataset = createDataset();
        }

        // Workaround Scrolling Bug When Header Is Disabled
        if (!hasHeader()) {
            registerAdapterDataObserver(scrollingBugWorkaround);
        }

        // Check
        this.parent = parent;
        parent.post(() -> {
            if (prerequisites == null) {
                throw new RuntimeException();
            }
        });
    }

    /**
     * Construct the dataset object. Override this to use a custom {@link FeedDataset} implementation.
     * @return The new dataset object
     */
    protected FeedDataset<T> createDataset() {
        return new SimpleFeedDataset<>();
    }

    /**
     * Handle prerequisites. Override this to attach prerequisite listeners.
     * @param prerequisites The new prerequisites
     */
    protected void handlePrerequisites(FeedPrerequisites prerequisites) {
        // General Instance Information
        prerequisites.require(FeedPrerequisite.Site.class);
        prerequisites.listen(prerequisite -> {
            if (prerequisite instanceof FeedPrerequisite.Site) {
                site = ((FeedPrerequisite.Site) prerequisite).get();
                // Update Permissions
                permissions.setSite(site);
                // Update Dataset
                notifier.change(viewModel.dataset.size());
                // Update Header
                if (hasHeader()) {
                    notifyItemChanged(0);
                }
            }
        });
    }

    /**
     * Add prerequisites.
     * @param prerequisites The new prerequisites
     */
    public void setPrerequisites(FeedPrerequisites prerequisites) {
        handlePrerequisites(prerequisites);

        // Check Status
        this.prerequisites = prerequisites;
        if (viewModel.dataset.size() > 0 && !arePrerequisitesLoaded()) {
            throw new RuntimeException();
        }
        if (prerequisites.isError()) {
            updateLoadingStatus(LoadingStatus.ERROR);
        }

        // Check If Everything Is Loaded
        prerequisites.listen(prerequisite -> {
            if (prerequisite == FeedPrerequisites.ERROR) {
                // Error
                if (arePrerequisitesLoaded()) {
                    // Don't Change State For Refresh Errors
                    Util.unknownError(parent.getContext());
                } else {
                    // Update Feed
                    updateLoadingStatus(LoadingStatus.ERROR);
                }
            } else if (prerequisite == FeedPrerequisites.COMPLETED) {
                // All Prerequisites Loaded
                startFirstPageLoadIfNeeded();
            } else if (prerequisite == FeedPrerequisites.RETRY_STARTED) {
                // Retry Started
                updateLoadingStatus(LoadingStatus.PENDING);
            }
        });
    }

    private void startFirstPageLoadIfNeeded() {
        if (viewModel.loadingStatus == LoadingStatus.PENDING && viewModel.nextPage == ViewModel.FIRST_PAGE) {
            assert viewModel.dataset.size() == 0;
            // Trigger Page Load
            load();
        }
    }

    /**
     * Check if all prerequisites are loaded.
     * @return True if all prerequisites are loaded, false otherwise
     */
    protected boolean arePrerequisitesLoaded() {
        return prerequisites != null && prerequisites.areLoaded();
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
            NextPageLoader loader = new NextPageLoader(parent.getContext(), null);
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
            loader.setLayoutParams(layoutParams);
            return new RecyclerView.ViewHolder(loader) {};
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
            assert arePrerequisitesLoaded();
            assert site != null;
            bindElement(holder, position);
        } else if (holder.getItemViewType() == ViewType.NEXT_PAGE.ordinal()) {
            NextPageLoader nextPageLoader = (NextPageLoader) holder.itemView;
            nextPageLoader.setup(viewModel.loadingStatus.displayMode, this::load);
        } else if (holder.getItemViewType() == ViewType.HEADER.ordinal()) {
            bindHeader(holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        // Items In Dataset
        int count = viewModel.dataset.size();
        // Next Page Loader
        count++;
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
            assert arePrerequisitesLoaded();
            return ViewType.ELEMENT.ordinal();
        }
    }

    private int getNextPageLoaderPosition() {
        int position = viewModel.dataset.size();
        if (hasHeader()) {
            position++;
        }
        return position;
    }

    private void updateLoadingStatus(LoadingStatus loadingStatus) {
        // Set Status
        LoadingStatus oldLoadingStatus = viewModel.loadingStatus;
        viewModel.loadingStatus = loadingStatus;

        // Check If Status Has Changed
        if (loadingStatus.displayMode != oldLoadingStatus.displayMode) {
            int position = getNextPageLoaderPosition();
            notifyItemChanged(position);
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
     * Check if this feed is split into virtual "pages."
     * @return False if it is paginated, true otherwise
     */
    protected boolean isSinglePage() {
        return false;
    }

    private void addPage(List<T> elements) {
        viewModel.nextPage++;
        int size = elements.size();
        assert size <= Util.ELEMENTS_PER_PAGE;
        viewModel.dataset.add(notifier, elements, false);
        LoadingStatus newStatus;
        if (isSinglePage() || size < Util.ELEMENTS_PER_PAGE) {
            // No More Elements To Load
            newStatus = LoadingStatus.DONE;
        } else {
            // More Elements Available
            newStatus = LoadingStatus.PENDING;
        }
        updateLoadingStatus(newStatus);
    }

    private boolean checkPrerequisites() {
        // Check If Prerequisites Are Loaded
        if (!arePrerequisitesLoaded()) {
            if (viewModel.loadingStatus == LoadingStatus.ERROR) {
                // Retry Failed Prerequisites
                assert prerequisites.isError();
                prerequisites.retry(connection);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Load more elements.
     */
    void load() {
        // Check If Prerequisites Are Loaded
        if (checkPrerequisites()) {
            return;
        }

        // Check Status
        if (viewModel.loadingStatus != LoadingStatus.PENDING && viewModel.loadingStatus != LoadingStatus.ERROR) {
            return;
        }
        if (isSinglePage() && viewModel.nextPage != ViewModel.FIRST_PAGE) {
            throw new RuntimeException();
        }
        updateLoadingStatus(LoadingStatus.LOADING);

        // Disable Previous Callbacks
        feedVersion++;

        // Load Page
        int oldVersion = feedVersion;
        loadPage(viewModel.nextPage, data -> {
            if (feedVersion == oldVersion) {
                addPage(data);
            }
        }, () -> {
            if (feedVersion == oldVersion) {
                updateLoadingStatus(LoadingStatus.ERROR);
            }
        });
    }

    /**
     * Load more elements if necessary.
     * @param layoutManager The feed's layout manager
     */
    public void checkLoadingStatus(LinearLayoutManager layoutManager) {
        if (getItemViewType(layoutManager.findLastVisibleItemPosition()) == ViewType.NEXT_PAGE.ordinal() && viewModel.loadingStatus == LoadingStatus.PENDING) {
            // Trigger Page Load
            load();
        }
    }

    /**
     * Refresh the adapter.
     * @param hard True if currently loaded content should disappear during the refresh, false otherwise
     * @param refreshPrerequisites True if prerequisites should also be refreshed, false otherwise
     * @param callback Callback that is executed on completion
     */
    public void refresh(boolean hard, boolean refreshPrerequisites, Runnable callback) {
        // Check If Prerequisites Are Loaded
        if (checkPrerequisites()) {
            callback.run();
            return;
        }

        // Refresh Prerequisites
        if (refreshPrerequisites) {
            prerequisites.refresh(connection);
        }

        // Different Refresh Types
        if (hard) {
            // Hard Refresh
            viewModel.clear(this);
            callback.run();
            startFirstPageLoadIfNeeded();
        } else {
            // Soft Refresh
            loadPage(ViewModel.FIRST_PAGE, data -> {
                // Add First Page
                viewModel.clear(this);
                addPage(data);

                // Callback
                callback.run();
            }, () -> {
                // Show Error
                viewModel.clear(this);
                updateLoadingStatus(LoadingStatus.ERROR);

                // Callback
                callback.run();
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        scrollingBugWorkaround.recyclerViews.add(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        scrollingBugWorkaround.recyclerViews.remove(recyclerView);
    }

    /**
     * Override this to handle when an element is edited. This element may or may not be in this feed.
     * @param element The edited element
     */
    public void handleEdit(Object element) {
    }
}
