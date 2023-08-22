package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ScaryPreference extends Preference {
    public ScaryPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // Get Text Color
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true);
        @ColorInt int textColor = ContextCompat.getColor(getContext(), typedValue.resourceId);

        // Disabled
        if (!isEnabled()) {
            getContext().getResources().getValue(com.google.android.material.R.dimen.material_emphasis_disabled, typedValue, true);
            float alpha = typedValue.getFloat();
            textColor = Color.argb((int) (alpha * 255), Color.red(textColor), Color.green(textColor), Color.blue(textColor));
        }

        // Set Text Color
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        title.setTextColor(textColor);
    }
}
