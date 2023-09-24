package com.thebrokenrail.combustible.util.info;

import android.content.Context;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatActivity;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;

/**
 * Simple dialog for showing information.
 */
public class InfoDialog {
    private final AppCompatActivity context;
    final Connection connection;
    private final String key;

    String text = "";
    CommunityView community = null;

    private boolean setup = false;

    InfoDialog(AppCompatActivity context, Connection connection, String key) {
        this.context = context;
        this.connection = connection;
        this.key = key;
    }

    /**
     * Set dialog information from a {@link CommunityView}.
     * @param context The Android context
     * @param community The community
     */
    public void set(Context context, CommunityView community) {
        Resources resources = context.getResources();
        set(community.community.description, new CommunityOrSiteCounts.Community(resources, community), resources);
        this.community = community;
    }

    /**
     * Set dialog information from a {@link GetSiteResponse}.
     * @param context The Android context
     * @param site General instance information
     */
    public void set(Context context, GetSiteResponse site) {
        Resources resources = context.getResources();
        set(site.site_view.site.sidebar, new CommunityOrSiteCounts.Site(resources, site), resources);
        this.community = null;
    }

    private void set(String description, CommunityOrSiteCounts counts, Resources resources) {
        // Add Counts
        String text = counts.toString();
        // Add Counts Header
        text = resources.getString(R.string.info_header_counts) + "\n\n" + text;
        // Trim
        text = text.trim();

        // Add Text
        if (description != null) {
            description = description.trim();
            if (description.length() > 0) {
                // Add Header
                text += "\n\n" + resources.getString(R.string.info_header_description) + "\n\n";
                // Add Text
                text += description;
                // Trim
                text = text.trim();
            }
        }

        // Set Text
        set(text);
    }

    /**
     * Set dialog information from raw text.
     * @param text The text
     */
    public void set(String text) {
        if (text != null) {
            // Trim
            text = text.trim();
            // Set
            if (text.length() > 0) {
                this.text = text;
                setup = true;
            }
        }
    }

    /**
     * Check if this dialog is setup.
     * @return True if it is setup, false otherwise
     */
    public boolean isSetup() {
        return setup;
    }

    /**
     * Show dialog.
     */
    public void show() {
        new InfoDialogFragment().show(context.getSupportFragmentManager(), key);
    }
}
