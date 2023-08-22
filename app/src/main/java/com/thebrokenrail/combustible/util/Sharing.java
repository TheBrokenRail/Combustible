package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.content.Intent;

import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.api.method.PersonView;

public class Sharing {
    public static String POST_PREFIX = "post";
    public static String COMMUNITY_PREFIX = "c";
    public static String COMMENT_PREFIX = "comment";
    public static String USER_PREFIX = "u";

    private static void share(Context context, String segment) {
        // Build URL
        String url = Links.relativeToInstance(context, segment);

        // Launch
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    public static void sharePost(Context context, int id) {
        share(context, POST_PREFIX + "/" + id);
    }

    public static void shareCommunity(Context context, CommunityView community) {
        String name = Util.getCommunityName(community.community);
        share(context, COMMUNITY_PREFIX + "/" + name);
    }

    public static void shareComment(Context context, int id) {
        share(context, COMMENT_PREFIX + "/" + id);
    }

    public static void sharePerson(Context context, PersonView person) {
        String name = Util.getPersonName(person.person);
        share(context, USER_PREFIX + "/" + name);
    }
}
