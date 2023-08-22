package com.thebrokenrail.combustible.util.markdown;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;

import androidx.annotation.NonNull;
import androidx.core.text.util.LinkifyCompat;

import org.commonmark.node.Link;

import java.util.regex.Pattern;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.RenderProps;
import io.noties.markwon.SpanFactory;
import io.noties.markwon.SpannableBuilder;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.CoreProps;

/**
 * Port of Jerboa's <a href="https://github.com/dessalines/jerboa/blob/ce5e8ba4c49cd1a5efd447df6929f22cf4cf9159/app/src/main/java/com/jerboa/util/MarkwonLemmyLinkPlugin.kt">MarkwonLemmyLinkPlugin</a> to Java.
 */
public class LemmyLinkPlugin extends AbstractMarkwonPlugin {
    /**
     * Pattern that matches all valid communities; intended to be loose.
     */
    private static final String COMMUNITY_PATTERN_FRAGMENT = "[a-zA-Z0-9_]{3,}";

    /**
     * Pattern to match all valid instances.
     */
    private static final String INSTANCE_PATTERN_FRAGMENT = "([a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]\\.)+[a-zA-Z]{2,}";

    /**
     * Pattern to match all valid usernames.
     */
    private static final String USER_PATTERN_FRAGMENT = "[a-zA-Z0-9_]{3,}";

    /**
     * Pattern to match Lemmy's unique community pattern, e.g. !community[@instance].
     */
    private static final Pattern LEMMY_COMMUNITY_PATTERN = Pattern.compile("(?<!\\S)!(" + COMMUNITY_PATTERN_FRAGMENT + ")(?:@(" + INSTANCE_PATTERN_FRAGMENT + "))?\\b");

    /**
     * Pattern to match Lemmy's unique user pattern, e.g. @user[@instance].
     */
    private static final Pattern LEMMY_USER_PATTERN = Pattern.compile("(?<!\\S)@(" + USER_PATTERN_FRAGMENT + ")(?:@(" + INSTANCE_PATTERN_FRAGMENT + "))?\\b");

    @Override
    public void configure(@NonNull Registry registry) {
        super.configure(registry);
        registry.require(CorePlugin.class, corePlugin -> corePlugin.addOnTextAddedListener(new LemmyTextAddedListener()));
    }

    private static class LemmyTextAddedListener implements CorePlugin.OnTextAddedListener {
        @Override
        public void onTextAdded(@NonNull MarkwonVisitor visitor, @NonNull String text, int start) {
            // we will be using the link that is used by markdown (instead of directly applying URLSpan)
            SpanFactory spanFactory = visitor.configuration().spansFactory().get(Link.class);
            if (spanFactory == null) {
                return;
            }

            // don't re-use builder (thread safety achieved for
            // render calls from different threads and ... better performance)
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            if (addLinks(builder)) {
                // target URL span specifically
                URLSpan[] spans = builder.getSpans(0, builder.length(), URLSpan.class);
                if (spans != null && spans.length > 0) {
                    RenderProps renderProps = visitor.renderProps();
                    SpannableBuilder spannableBuilder = visitor.builder();
                    for (URLSpan span : spans) {
                        CoreProps.LINK_DESTINATION.set(renderProps, span.getURL());
                        SpannableBuilder.setSpans(spannableBuilder, spanFactory.getSpans(visitor.configuration(), renderProps), start + builder.getSpanStart(span), start + builder.getSpanEnd(span));
                    }
                }
            }
        }

        private boolean addLinks(Spannable text) {
            boolean communityLinkAdded = LinkifyCompat.addLinks(text, LEMMY_COMMUNITY_PATTERN, null);
            boolean userLinkAdded = LinkifyCompat.addLinks(text, LEMMY_USER_PATTERN, null);

            return communityLinkAdded || userLinkAdded;
        }
    }
}
