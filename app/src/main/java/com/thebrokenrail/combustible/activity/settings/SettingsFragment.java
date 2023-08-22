package com.thebrokenrail.combustible.activity.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.util.Util;

public abstract class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Edge-To-Edge
        RecyclerView list = getListView();
        ViewCompat.setOnApplyWindowInsetsListener(list, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            list.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });

        // App Bar
        AppBarLayout appBarLayout = requireActivity().findViewById(R.id.app_bar_layout);
        Util.updateAppBarLift(appBarLayout, list);
    }
}
