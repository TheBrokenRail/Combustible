package com.thebrokenrail.combustible.activity.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.util.EdgeToEdge;
import com.thebrokenrail.combustible.util.Util;

public abstract class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Edge-To-Edge
        RecyclerView list = getListView();
        EdgeToEdge.setupScroll(list);

        // App Bar
        AppBarLayout appBarLayout = requireActivity().findViewById(R.id.app_bar_layout);
        Util.updateAppBarLift(appBarLayout, list);
    }
}
