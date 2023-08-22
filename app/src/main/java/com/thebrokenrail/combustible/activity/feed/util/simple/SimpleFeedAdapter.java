package com.thebrokenrail.combustible.activity.feed.util.simple;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.SortableFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.widget.CommonIcons;
import com.thebrokenrail.combustible.widget.LinkWithIcon;
import com.thebrokenrail.combustible.widget.Sorter;

/**
 * {@link SortableFeedAdapter} for extremely simple elements.
 * @param <T> Type of element in list
 */
abstract class SimpleFeedAdapter<T> extends SortableFeedAdapter<T> {
    private static class SimpleViewHolder extends RecyclerView.ViewHolder {
        private final LinkWithIcon link;
        private final CommonIcons icons;

        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);
            link = itemView.findViewById(R.id.simple_item);
            Resources resources = itemView.getResources();
            float linkWithIconFontSize = resources.getDimension(R.dimen.link_with_icon_font_size);
            float postTitleFontSize = resources.getDimension(R.dimen.post_title_font_size);
            link.setSizeMultiplier(postTitleFontSize / linkWithIconFontSize);
            icons = itemView.findViewById(R.id.simple_item_icons);
        }
    }

    public SimpleFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
    }

    @Override
    protected final View createHeader(ViewGroup parent) {
        Sorter sorter = new Sorter(parent.getContext(), null);
        sorter.setId(R.id.feed_sorter);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        sorter.setLayoutParams(layoutParams);
        return sorter;
    }

    @Override
    protected final RecyclerView.ViewHolder createItem(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.simple_item, parent, false);
        return new SimpleViewHolder(root);
    }

    @Override
    protected final void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Avatars
        boolean showAvatars = true;
        if (site.my_user != null) {
            showAvatars = site.my_user.local_user_view.local_user.show_avatars;
        }

        // Bind
        T obj = viewModel.dataset.get(position);
        SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
        simpleViewHolder.link.setup(showAvatars ? getIcon(obj) : null, false, getName(obj), null);
        simpleViewHolder.itemView.setOnClickListener(view -> click(view.getContext(), obj));
        setupIcons(simpleViewHolder.icons, obj);
    }

    /**
     * Get the name of an element.
     * @param obj The element
     * @return The element's name
     */
    protected abstract String getName(T obj);

    /**
     * Get the icon of an element
     * @param obj The element
     * @return The icon's URL (or null if there is no icon)
     */
    protected abstract String getIcon(T obj);

    /**
     * Click an element.
     * @param context The Android context
     * @param obj The element to click
     */
    protected abstract void click(Context context, T obj);

    /**
     * Setup icons on element.
     * @param icons The icons widget
     * @param obj The element to use
     */
    protected abstract void setupIcons(CommonIcons icons, T obj);
}
