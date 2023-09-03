package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.SortType;

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
        public <T extends Enum<?>> void set(T value) {
            if (isSetup) {
                return;
            }
            setForce(value);
        }

        @SuppressWarnings({"unchecked"})
        private <T extends Enum<?>> void setForce(T value) {
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

        private boolean isSetup = false;

        /**
         * Mark as setup.
         */
        public void setup() {
            isSetup = true;
        }
    }

    private ViewModel viewModel = new ViewModel();
    private boolean hasToken = false;
    private Runnable refresh = () -> {};

    private final Map<Class<? extends Enum<?>>, TextInputLayout> spinners = new HashMap<>();

    private final int margin;

    public Sorter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        // Margin
        margin = getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
        setPaddingRelative(margin, 0, margin, 0);

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

        // Create Text Input Layout
        TextInputLayout textInputLayout = new TextInputLayout(getContext(), null, com.google.android.material.R.attr.textInputOutlinedExposedDropdownMenuStyle);
        textInputLayout.setHint(label);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, margin, 0, 0);
        textInputLayout.setLayoutParams(layoutParams);
        addView(textInputLayout);
        spinners.put(type, textInputLayout);

        // Create Spinner
        MaterialAutoCompleteTextView spinner = new MaterialAutoCompleteTextView(textInputLayout.getContext());
        spinner.setInputType(InputType.TYPE_NULL);
        TextInputLayout.LayoutParams spinnerLayoutParams = new TextInputLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        spinner.setLayoutParams(spinnerLayoutParams);
        textInputLayout.addView(spinner);

        // Create Adapter
        String[] simpleItems = new String[valuesArray.length];
        for (int i = 0; i < valuesArray.length; i++) {
            simpleItems[i] = String.valueOf(valuesArray[i]);
        }
        spinner.setSimpleItems(simpleItems);
        WrappedListAdapter adapter = new WrappedListAdapter((ArrayAdapter<?>) spinner.getAdapter()) {
            @Override
            public boolean isEnabled(int position) {
                if (type == ListingType.class && position == ListingType.Subscribed.ordinal()) {
                    return hasToken;
                } else {
                    return super.isEnabled(position);
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setEnabled(isEnabled(position));
                return view;
            }
        };
        spinner.setAdapter(adapter);

        // Handle Item Selection
        spinner.setOnItemClickListener((parent, view, position, id) -> {
            Enum<?> newValue = Objects.requireNonNull(type.getEnumConstants())[position];
            Enum<?> value = viewModel.get(type);
            if (newValue != value) {
                viewModel.setForce(newValue);
                refresh.run();
            }
        });
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
        for (Map.Entry<Class<? extends Enum<?>>, TextInputLayout> entry : spinners.entrySet()) {
            // Set Visibility
            boolean visible = isVisible.apply(entry.getKey());
            TextInputLayout textInputLayout = entry.getValue();
            textInputLayout.setVisibility(visible ? VISIBLE : GONE);
            if (visible) {
                visibleCount++;
            }

            // Set Value
            if (visible) {
                MaterialAutoCompleteTextView spinner = (MaterialAutoCompleteTextView) textInputLayout.getEditText();
                assert spinner != null;
                Enum<?> value = viewModel.get(entry.getKey());
                CharSequence valueStr = (CharSequence) spinner.getAdapter().getItem(value.ordinal());
                spinner.setText(valueStr, false);
            }
        }
        setEnabled(viewModel.isSetup);
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
        for (TextInputLayout textInputLayout : spinners.values()) {
            textInputLayout.setEnabled(enabled);
        }
    }
}
