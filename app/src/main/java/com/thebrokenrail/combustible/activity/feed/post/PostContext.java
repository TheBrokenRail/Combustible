package com.thebrokenrail.combustible.activity.feed.post;

import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.util.markdown.Markdown;

public interface PostContext {
    enum PinMode {
        HOME,
        COMMUNITY,
        NONE
    }

    Connection getConnection();
    GetSiteResponse getSite();
    boolean showCreator();
    boolean showCommunity();
    PinMode getPinMode();
    void replace(PostView oldElement, PostView newElement);
    boolean showText();
    Markdown getMarkdown();
}
