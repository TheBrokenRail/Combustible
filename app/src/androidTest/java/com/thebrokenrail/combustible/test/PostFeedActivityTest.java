package com.thebrokenrail.combustible.test;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.test.util.FeedActivityTest;
import com.thebrokenrail.combustible.test.util.OkHttpIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import okhttp3.OkHttpClient;

@LargeTest
public class PostFeedActivityTest {
    @Rule
    public final ActivityScenarioRule<PostFeedActivity> activityTestRule = new ActivityScenarioRule<>(PostFeedActivity.class);

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
}
