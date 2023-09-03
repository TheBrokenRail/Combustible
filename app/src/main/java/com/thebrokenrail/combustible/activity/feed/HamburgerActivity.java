package com.thebrokenrail.combustible.activity.feed;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.LemmyActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.blocked.BlockedFeedActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.inbox.InboxFeedActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.saved.SavedFeedActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.user.UserFeedActivity;
import com.thebrokenrail.combustible.activity.fullscreen.login.LoginActivity;
import com.thebrokenrail.combustible.activity.settings.app.AppSettingsActivity;
import com.thebrokenrail.combustible.activity.settings.user.UserSettingsActivity;
import com.thebrokenrail.combustible.api.method.BlockCommunity;
import com.thebrokenrail.combustible.util.config.Config;
import com.thebrokenrail.combustible.util.InfoDialog;
import com.thebrokenrail.combustible.util.Links;
import com.thebrokenrail.combustible.util.MenuItemTarget;
import com.thebrokenrail.combustible.util.Util;

/**
 * Activity with a hamburger menu.
 */
class HamburgerActivity extends LemmyActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Drawer
    private static final int DRAWER_GRAVITY = GravityCompat.END;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Community/Legal Info
    public InfoDialog.Manager infoManager;
    protected InfoDialog infoLegal;
    protected InfoDialog infoCommunity;

    // View Profile
    protected Integer currentUser = null;

    // Community Blocking
    protected boolean canBlockCommunity = false;
    protected Boolean isCommunityBlocked = null;

    // View Profile Icon
    protected MenuItemTarget viewProfileTarget = null;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // Dialogs
        infoManager = new InfoDialog.Manager(this, connection);
        infoLegal = infoManager.create("legal");
        infoCommunity = infoManager.create("community");

        // Setup Menu
        navigationView = findViewById(R.id.feed_menu);
        updateNavigation();
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.feed_drawer_layout);
        viewProfileTarget = new MenuItemTarget(navigationView.getMenu().findItem(R.id.feed_menu_view_profile));

        // Handle Back
        OnBackPressedCallback backCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                // Close Drawer
                drawerLayout.closeDrawer(DRAWER_GRAVITY);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Enable Callback
                backCallback.setEnabled(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Disable Callback
                backCallback.setEnabled(false);
            }
        });
    }

    /**
     * Update navigation menu.
     */
    protected void updateNavigation() {
        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.feed_menu_login).setVisible(!connection.hasToken());
        menu.findItem(R.id.feed_menu_view_profile).setVisible(connection.hasToken());
        menu.findItem(R.id.feed_menu_view_profile).setEnabled(currentUser != null);
        menu.findItem(R.id.feed_menu_logout).setVisible(connection.hasToken());
        menu.findItem(R.id.feed_menu_register).setVisible(!connection.hasToken());
        menu.setGroupVisible(R.id.feed_menu_group_saved, connection.hasToken());
        menu.findItem(R.id.feed_menu_community_info).setEnabled(infoCommunity.isSetup());
        menu.findItem(R.id.feed_menu_legal_info).setEnabled(infoLegal.isSetup());
        menu.findItem(R.id.feed_menu_user_settings).setVisible(connection.hasToken());
        menu.setGroupVisible(R.id.feed_menu_group_block, canBlockCommunity);
        menu.findItem(R.id.feed_menu_block_community).setEnabled(isCommunityBlocked != null);
        menu.findItem(R.id.feed_menu_block_community).setTitle((isCommunityBlocked != null && isCommunityBlocked) ? R.string.feed_menu_unblock_community : R.string.feed_menu_block_community);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_menu_app_settings) {
            Intent intent = new Intent(this, AppSettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_register) {
            String url = Links.relativeToInstance(this, "signup");
            Links.open(this, url);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_logout) {
            Config config = Config.create(this);
            config.setToken(null);
            fullRecreate();
            return true;
        } else if (item.getItemId() == R.id.feed_menu_saved) {
            Intent intent = new Intent(this, SavedFeedActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_blocked) {
            Intent intent = new Intent(this, BlockedFeedActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_view_profile) {
            Intent intent = new Intent(this, UserFeedActivity.class);
            intent.putExtra(UserFeedActivity.USER_ID_EXTRA, currentUser);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_community_info) {
            infoCommunity.show();
            return true;
        } else if (item.getItemId() == R.id.feed_menu_legal_info) {
            infoLegal.show();
            return true;
        } else if (item.getItemId() == R.id.feed_menu_user_settings) {
            Intent intent = new Intent(this, UserSettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_block_community) {
            BlockCommunity method = blockCommunity(!isCommunityBlocked);
            connection.send(method, blockCommunityResponse -> fullRecreate(), () -> Util.unknownError(HamburgerActivity.this));
            return true;
        } else if (item.getItemId() == R.id.feed_menu_inbox) {
            Intent intent = new Intent(this, InboxFeedActivity.class);
            startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_open_menu) {
            drawerLayout.openDrawer(DRAWER_GRAVITY);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Update community block state.
     * @param shouldBlock New state
     * @return The API method to call.
     */
    protected BlockCommunity blockCommunity(boolean shouldBlock) {
        throw new RuntimeException();
    }
}
