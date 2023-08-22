package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.ArrayRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.SortType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Widget that shows sorting options.
 */
public class Sorter extends LinearLayout {
    /**
     * The widget's view model.
     */
    public static class ViewModel {
        private final Map<Class<? extends Enum<?>>, Enum<?>> data = new HashMap<>();

        /**
         * Get the specified value.
         * @param t The type of value
         * @return The value
         * @param <T> The value's class
         */
        @SuppressWarnings({"unchecked"})
        public <T extends Enum<?>> T get(Class<T> t) {
            if (data.containsKey(t)) {
                return (T) data.get(t);
            } else {
                Enum<?> value = getDefault(t);
                data.put(t, value);
                return (T) value;
            }
        }

        /**
         * Set the specified value
         * @param value The new value
         * @param <T> The value's type
         */
        @SuppressWarnings({"unchecked"})
        public <T extends Enum<?>> void set(T value) {
            data.put((Class<? extends Enum<?>>) value.getClass(), value);
        }

        private static Enum<?> getDefault(Class<? extends Enum<?>> type) {
            if (type == ListingType.class) {
                return ListingType.Local;
            } else if (type == SortType.class) {
                return SortType.Active;
            } else if (type == CommentSortType.class) {
                return CommentSortType.Hot;
            } else {
                throw new RuntimeException();
            }
        }
    }

    private ViewModel viewModel = new ViewModel();
    private boolean hasToken = false;
    private Runnable refresh = () -> {};

    private final Map<Class<? extends Enum<?>>, AppCompatSpinner> spinners = new HashMap<>();

    public Sorter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        // Add Enums
        add(SortType.class, R.string.posts_sort_by, R.array.sort_types);
        add(ListingType.class, R.string.posts_listing_type, R.array.listing_types);
        add(CommentSortType.class, R.string.posts_sort_by, R.array.comment_sort_types);
        bind(viewModel, hasToken, refresh, type -> true);
    }

    private void add(Class<? extends Enum<?>> type, @StringRes int label, @ArrayRes int values) {
        // Check
        CharSequence[] valuesArray = getResources().getTextArray(values);
        assert valuesArray.length == Objects.requireNonNull(type.getEnumConstants()).length;

        // Create Spinner
        @LayoutRes int textViewResId = android.R.layout.simple_spinner_item;
        @LayoutRes int dropDownViewResource = android.R.layout.simple_spinner_dropdown_item;
        AppCompatSpinner spinner = new AppCompatSpinner(getContext());
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(), textViewResId, 0, Arrays.asList(valuesArray)) {
            @Override
            public boolean isEnabled(int position) {
                if (type == ListingType.class && position == ListingType.Subscribed.ordinal()) {
                    return hasToken;
                } else {
                    return super.isEnabled(position);
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setEnabled(isEnabled(position));
                return view;
            }
        };
        adapter.setDropDownViewResource(dropDownViewResource);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Enum<?> newValue = Objects.requireNonNull(type.getEnumConstants())[position];
                Enum<?> value = viewModel.get(type);
                if (newValue != value) {
                    viewModel.set(newValue);
                    refresh.run();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        int spinnerId = View.generateViewId();
        spinner.setId(spinnerId);
        spinners.put(type, spinner);

        // Create Label
        AppCompatTextView labelView = new AppCompatTextView(getContext());
        labelView.setText(label);
        labelView.setLabelFor(spinnerId);

        // Inner Layout
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int margin = getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
        layoutParams.setMargins(margin, margin, margin, 0);
        layout.setLayoutParams(layoutParams);

        // Layout Label
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelView.setLayoutParams(layoutParams);
        layout.addView(labelView);

        // Layout Spinner
        layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        spinner.setLayoutParams(layoutParams);
        layout.addView(spinner);

        // Finish
        addView(layout);
    }

    /**
     * Bind this widget to a view model.
     * @param viewModel The new view model
     * @param hasToken If the user is logged in
     * @param refresh Callback that is executed on refresh
     * @param isVisible Function that checks if a spinner should be visible
     */
    public void bind(ViewModel viewModel, boolean hasToken, Runnable refresh, Function<Class<? extends Enum<?>>, Boolean> isVisible) {
        this.viewModel = viewModel;
        this.hasToken = hasToken;
        this.refresh = refresh;
        int visibleCount = 0;
        for (Map.Entry<Class<? extends Enum<?>>, AppCompatSpinner> entry : spinners.entrySet()) {
            // Set Visibility
            boolean visible = isVisible.apply(entry.getKey());
            AppCompatSpinner spinner = entry.getValue();
            LinearLayout parent = (LinearLayout) spinner.getParent();
            parent.setVisibility(visible ? VISIBLE : GONE);
            if (visible) {
                visibleCount++;
            }

            // Set Value
            if (visible) {
                Enum<?> value = viewModel.get(entry.getKey());
                spinner.setSelection(value.ordinal());
            }
        }
        if (visibleCount == 0) {
            // 0-height Sorters can cause issues: https://stackoverflow.com/q/40787577/16198887
            throw new RuntimeException();
        }
    }

    /**
     * Set if this widget is enabled.
     * @param enabled If the widget should be enabled
     */
    public void setEnabled(boolean enabled) {
        for (AppCompatSpinner spinner : spinners.values()) {
            spinner.setEnabled(enabled);
        }
    }
}
