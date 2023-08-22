package com.thebrokenrail.combustible.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.ColorInt;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.comment.CommentFeedActivity;
import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.user.UserFeedActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetCommunity;
import com.thebrokenrail.combustible.api.method.GetPersonDetails;

import java.util.function.Consumer;

import okhttp3.HttpUrl;

public class Links {
    public static String relativeToInstance(Context context, String segment) {
        Config config = new Config(context);
        HttpUrl url = config.getInstance();
        url = url.newBuilder().addPathSegments(segment).build();
        return url.toString();
    }

    // https://github.com/dessalines/jerboa/blob/ce5e8ba4c49cd1a5efd447df6929f22cf4cf9159/app/src/main/java/com/jerboa/Utils.kt#L303-L342
    private static String standardizeUrl(Context context, String url) {
        if (url.startsWith("https://") || url.startsWith("http://")) {
            HttpUrl parsedUrl = HttpUrl.parse(url);
            return parsedUrl != null ? parsedUrl.toString() : null;
        } else if (url.startsWith("/c/") || url.startsWith("/u/")) {
            url = url.substring(1);
            return relativeToInstance(context, url);
        } else if (url.startsWith("!")) {
            url = url.substring(1);
            return relativeToInstance(context, "c/" + url);
        } else if (url.startsWith("@")) {
            url = url.substring(1);
            return relativeToInstance(context, "u/" + url);
        } else {
            return null;
        }
    }

    private static <T> void sendMethod(Context context, Connection.Method<T> method, Consumer<T> callback) {
        assert context.getApplicationContext() == context;
        Config config = new Config(context);
        Connection connection = new Connection(config.getInstance());
        connection.setToken(config.getToken());
        Handler handler = new Handler(Looper.getMainLooper());
        connection.setCallbackHelper(handler::post);
        connection.send(method, callback, () -> Util.unknownError(context));
    }

    private static String stripOtherSegments(String url) {
        int index = url.indexOf('/');
        if (index != -1) {
            return url.substring(0, index);
        } else {
            return url;
        }
    }

    private static boolean openLemmy(Context context, String url) {
        String userPrefix = relativeToInstance(context, Sharing.USER_PREFIX + "/");
        String communityPrefix = relativeToInstance(context, Sharing.COMMUNITY_PREFIX + "/");
        String commentPrefix = relativeToInstance(context, Sharing.COMMENT_PREFIX + "/");
        String postPrefix = relativeToInstance(context, Sharing.POST_PREFIX + "/");
        if (url.startsWith(userPrefix)) {
            // User
            url = url.substring(userPrefix.length());
            url = stripOtherSegments(url);
            GetPersonDetails method = new GetPersonDetails();
            method.limit = 1;
            method.username = url;
            sendMethod(context, method, getPersonDetailsResponse -> {
                Intent intent = new Intent(context, UserFeedActivity.class);
                intent.putExtra(UserFeedActivity.USER_ID_EXTRA, getPersonDetailsResponse.person_view.person.id);
                context.startActivity(intent);
            });
            return true;
        } else if (url.startsWith(communityPrefix)) {
            // Community
            url = url.substring(communityPrefix.length());
            url = stripOtherSegments(url);
            GetCommunity method = new GetCommunity();
            method.name = url;
            sendMethod(context, method, getCommunityResponse -> {
                Intent intent = new Intent(context, PostFeedActivity.class);
                intent.putExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, getCommunityResponse.community_view.community.id);
                context.startActivity(intent);
            });
            return true;
        } else if (url.startsWith(commentPrefix) || url.startsWith(postPrefix)) {
            // Comment Or Post
            boolean isComment = url.startsWith(commentPrefix);
            url = url.substring((isComment ? commentPrefix : postPrefix).length());
            url = stripOtherSegments(url);
            Intent intent = new Intent(context, CommentFeedActivity.class);
            String extra = isComment ? CommentFeedActivity.COMMENT_ID_EXTRA : CommentFeedActivity.POST_ID_EXTRA;
            intent.putExtra(extra, Integer.parseInt(url));
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    public static void open(Context context, String url) {
        if (url == null) {
            return;
        }
        url = standardizeUrl(context, url);
        if (url == null) {
            return;
        }
        if (openLemmy(context.getApplicationContext(), url)) {
            return;
        }

        // Chrome Custom Tab
        @ColorInt int colorPrimaryLight = ContextCompat.getColor(context, R.color.md_theme_light_primary);
        @ColorInt int colorPrimaryDark = ContextCompat.getColor(context, R.color.md_theme_dark_primary);
        CustomTabsIntent intent = new CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(new CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(colorPrimaryLight)
                        .build())
                .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, new CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(colorPrimaryDark)
                        .build())
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .build();

        // Launch
        try {
            intent.launchUrl(context, Uri.parse(url));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Util.unknownError(context);
        }
    }
}
