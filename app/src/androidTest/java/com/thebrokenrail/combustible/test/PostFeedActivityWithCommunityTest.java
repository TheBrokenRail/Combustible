package com.thebrokenrail.combustible.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import com.google.android.material.appbar.MaterialToolbar;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.test.util.FeedActivityTest;
import com.thebrokenrail.combustible.test.util.OkHttpIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import okhttp3.OkHttpClient;

@LargeTest
public class PostFeedActivityWithCommunityTest {
    private static final int TEST_COMMUNITY_ID = 5;

    private static final String TEST_COMMUNITY_TITLE = "Lemmy.world Announcements";
    private static final String TEST_COMMUNITY_NAME = "!lemmyworld";

    @Rule
    public final ActivityScenarioRule<PostFeedActivity> activityTestRule = new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), PostFeedActivity.class).putExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, TEST_COMMUNITY_ID));

    private OkHttpIdlingResource okHttpIdlingResource;

    @Before
    public void setup() {
        // Get Activity
        activityTestRule.getScenario().onActivity(activity -> {
            OkHttpClient client = activity.getConnection().getClient();
            // Register Idling Resource
            okHttpIdlingResource = new OkHttpIdlingResource(client);
            IdlingRegistry.getInstance().register(okHttpIdlingResource);
        });
    }

    @After
    public void cleanup() {
        // Unregister Idling Resource
        IdlingRegistry.getInstance().unregister(okHttpIdlingResource);
    }

    @Test
    public void feedTest() {
        FeedActivityTest.feedTest(true);
    }

    @Test
    public void hamburgerTest() {
        FeedActivityTest.hamburgerTest();
    }

    @Test
    public void toolbarTest() {
        onView(withId(R.id.toolbar))
                .check((view, noViewFoundException) -> {
                    // Get Toolbar
                    if (!(view instanceof MaterialToolbar)) {
                        throw noViewFoundException;
                    }
                    MaterialToolbar toolbar = (MaterialToolbar) view;
                    // Check Title
                    assertEquals(TEST_COMMUNITY_TITLE, toolbar.getTitle());
                    assertEquals(TEST_COMMUNITY_NAME, toolbar.getSubtitle());
                });
    }
}
