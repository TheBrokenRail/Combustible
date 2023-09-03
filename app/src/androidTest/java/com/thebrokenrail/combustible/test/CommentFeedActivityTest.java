package com.thebrokenrail.combustible.test;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import com.thebrokenrail.combustible.activity.feed.comment.CommentFeedActivity;
import com.thebrokenrail.combustible.test.util.FeedActivityTest;
import com.thebrokenrail.combustible.test.util.OkHttpIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import okhttp3.OkHttpClient;

@LargeTest
public class CommentFeedActivityTest {
    private static final int TEST_POST_ID = 3995057;

    @Rule
    public final ActivityScenarioRule<CommentFeedActivity> activityTestRule = new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), CommentFeedActivity.class).putExtra(CommentFeedActivity.POST_ID_EXTRA, TEST_POST_ID));

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
        FeedActivityTest.feedTest(false /* Not All Comments On First Page Can Be Displayed Due to Missing Parents */);
    }

    @Test
    public void hamburgerTest() {
        FeedActivityTest.hamburgerTest();
    }
}
