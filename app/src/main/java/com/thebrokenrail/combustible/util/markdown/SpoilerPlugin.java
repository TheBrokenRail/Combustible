package com.thebrokenrail.combustible.util.markdown;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.image.AsyncDrawableScheduler;

/**
 * Port of Jerboa's <a href="https://github.com/dessalines/jerboa/blob/295cd42480c53c56218e8f644283a52903d53e7e/app/src/main/java/com/jerboa/util/MarkwonSpoilerPlugin.kt">MarkwonSpoilerPlugin</a> to Java.
 */
public class SpoilerPlugin extends AbstractMarkwonPlugin {
    private static class SpoilerTitleSpan {
        private final CharSequence title;

        private SpoilerTitleSpan(CharSequence title) {
            this.title = title;
        }
    }

    private static class SpoilerCloseSpan {
    }

    private final boolean enableInteraction;

    public SpoilerPlugin(boolean enableInteraction) {
        this.enableInteraction = enableInteraction;
    }

    @Override
    public void configure(@NonNull Registry registry) {
        super.configure(registry);
        registry.require(CorePlugin.class, corePlugin -> corePlugin.addOnTextAddedListener(new SpoilerTextAddedListener()));
    }

    private static class SpoilerTextAddedListener implements CorePlugin.OnTextAddedListener {
        private static final Pattern spoilerTitleRegex = Pattern.compile("(:::\\s*spoiler\\s*)(.*)");
        private static final Pattern spoilerCloseRegex = Pattern.compile("^(?!.*spoiler).*:::");

        @Override
        public void onTextAdded(@NonNull MarkwonVisitor visitor, @NonNull String text, int start) {
            // Find all spoiler "start" lines
            Matcher spoilerTitles = spoilerTitleRegex.matcher(text);

            while (spoilerTitles.find()) {
                String spoilerTitle = spoilerTitles.group(2);
                visitor.builder().setSpan(new SpoilerTitleSpan(spoilerTitle), start, start + spoilerTitles.end(2), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            // Find all spoiler "end" lines
            Matcher spoilerCloses = spoilerCloseRegex.matcher(text);
            while (spoilerCloses.find()) {
                visitor.builder().setSpan(new SpoilerCloseSpan(), start, start + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        super.afterSetText(textView);
        try {
            SpannableStringBuilder spanned = new SpannableStringBuilder(textView.getText());
            SpoilerTitleSpan[] spoilerTitleSpans = spanned.getSpans(0, spanned.length(), SpoilerTitleSpan.class);
            SpoilerCloseSpan[] spoilerCloseSpans = spanned.getSpans(0, spanned.length(), SpoilerCloseSpan.class);

            Arrays.sort(spoilerTitleSpans, Comparator.comparingInt(spanned::getSpanStart));
            Arrays.sort(spoilerCloseSpans, Comparator.comparingInt(spanned::getSpanStart));

            for (int i = 0; i < spoilerTitleSpans.length; i++) {
                SpoilerTitleSpan spoilerTitleSpan = spoilerTitleSpans[i];

                int spoilerStart = spanned.getSpanStart(spoilerTitleSpan);

                int spoilerEnd = spanned.length();
                if (i < spoilerCloseSpans.length) {
                    SpoilerCloseSpan spoilerCloseSpan = spoilerCloseSpans[i];
                    spoilerEnd = spanned.getSpanEnd(spoilerCloseSpan);
                }

                final boolean[] open = {false};
                Function<Boolean, CharSequence> getSpoilerTitle = openParam -> {
                    CharSequence ret = spoilerTitleSpan.title;
                    if (openParam) {
                        ret = "▼ " + ret;
                    } else {
                        ret = "▶ " + ret;
                    }
                    return ret;
                };

                CharSequence spoilerTitle = getSpoilerTitle.apply(false);

                SpannableStringBuilder spoilerContent = (SpannableStringBuilder) spanned.subSequence(spanned.getSpanEnd(spoilerTitleSpan) + 1, spoilerEnd - 3);
                spoilerContent.insert(0, "\n");

                // Remove spoiler content from span
                spanned.replace(spoilerStart, spoilerEnd, spoilerTitle);
                // Set span block title
                spanned.setSpan(spoilerTitle, spoilerStart, spoilerStart + spoilerTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                ClickableSpan wrapper = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if (enableInteraction) {
                            textView.cancelPendingInputEvents();
                            open[0] = !open[0];

                            spanned.replace(spoilerStart, spoilerStart + spoilerTitle.length(), getSpoilerTitle.apply(open[0]));
                            if (open[0]) {
                                spanned.insert(spoilerStart + spoilerTitle.length(), spoilerContent);
                            } else {
                                spanned.replace(spoilerStart + spoilerTitle.length(), spoilerStart + spoilerTitle.length() + spoilerContent.length(), "");
                            }

                            textView.setText(spanned);
                            AsyncDrawableScheduler.schedule(textView);
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                    }
                };

                // Set spoiler block type as ClickableSpan
                spanned.setSpan(wrapper, spoilerStart, spoilerStart + spoilerTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                textView.setText(spanned);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
