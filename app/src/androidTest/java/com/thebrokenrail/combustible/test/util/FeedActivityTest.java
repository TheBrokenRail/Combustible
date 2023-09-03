package com.thebrokenrail.combustible.test.util;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.Menu;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.adapter.base.FeedAdapter;
import com.thebrokenrail.combustible.util.Util;

public class FeedActivityTest {
    public static void feedTest(boolean checkItemCount) {
        onView(withId(R.id.feed))
                .check((view, noViewFoundException) -> {
                    // Get Adapter
                    if (!(view instanceof RecyclerView)) {
                        throw noViewFoundException;
                    }
                    RecyclerView feed = (RecyclerView) view;
                    FeedAdapter<?> adapter = (FeedAdapter<?>) feed.getAdapter();
                    if (adapter == null) {
                        throw noViewFoundException;
                    }
                    // Check If Loaded
                    assertTrue(adapter.isFirstPageLoaded());
                    // Check Item Count
                    if (checkItemCount) {
                        int expectedItemCount = 1 /* Header */ + Util.ELEMENTS_PER_PAGE /* Posts */ + 1 /* Next Page Loader */;
                        int itemCount = adapter.getItemCount();
                        assertEquals(expectedItemCount, itemCount);
                    }
                });
    }

    public static void hamburgerTest() {
        onView(withId(R.id.feed_menu))
                .check((view, noViewFoundException) -> {
                    // Get Menu
                    if (!(view instanceof NavigationView)) {
                        throw noViewFoundException;
                    }
                    NavigationView navigationView = (NavigationView) view;
                    Menu menu = navigationView.getMenu();
                    // Check If Info Has Loaded
                    assertTrue(menu.findItem(R.id.feed_menu_community_info).isEnabled());
                    assertTrue(menu.findItem(R.id.feed_menu_legal_info).isEnabled());
                });
    }
}
