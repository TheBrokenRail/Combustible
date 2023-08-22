package com.thebrokenrail.combustible.util.markdown;

import android.content.Context;
import android.content.res.Configuration;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.thebrokenrail.combustible.util.Links;
import com.thebrokenrail.combustible.util.NiceLinkMovementMethod;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import io.noties.markwon.syntax.Prism4jTheme;
import io.noties.markwon.syntax.Prism4jThemeDarkula;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.PrismBundle;

@PrismBundle(includeAll = true)
public class Markdown {
    private final Markwon markwon;

    public Markdown(Context context) {
        Prism4j prism4j = new Prism4j(new GrammarLocatorDef());
        Prism4jTheme prism4jTheme = isDarkMode(context) ? Prism4jThemeDarkula.create() : Prism4jThemeDefault.create();
        this.markwon = Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(new CustomImagePlugin(context))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS, true)) // email urls interfere with lemmy links
                .usePlugin(new LemmyLinkPlugin())
                .usePlugin(MovementMethodPlugin.create(NiceLinkMovementMethod.getInstance()))
                .usePlugin(SyntaxHighlightPlugin.create(prism4j, prism4jTheme))
                .usePlugin(new SpoilerPlugin(true))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        super.configureConfiguration(builder);
                        // Custom Tabs
                        builder.linkResolver((view, link) -> Links.open(view.getContext(), link));
                    }
                })
                .build();
    }

    private static boolean isDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
        }
        return false;
    }

    public void set(TextView view, String text) {
        markwon.setMarkdown(view, text);
        // Reverse TextView.fixFocusableAndClickableSettings()
        view.setClickable(false);
        view.setLongClickable(false);
    }
}
