package com.thebrokenrail.combustible.util;

import android.graphics.RectF;
import android.graphics.drawable.RippleDrawable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.thebrokenrail.combustible.util.markdown.image.MarkdownImageSpan;

import io.noties.markwon.ext.tables.TableRowSpan;

public class NiceLinkMovementMethod extends LinkMovementMethod {
    private static NiceLinkMovementMethod singleInstance;

    public static NiceLinkMovementMethod getInstance() {
        if (singleInstance == null) {
            singleInstance = new NiceLinkMovementMethod();
        }
        return singleInstance;
    }

    public static void setup(TextView textView) {
        textView.setMovementMethod(getInstance());
        // Override URLSpan
        SpannableStringBuilder text = SpannableStringBuilder.valueOf(textView.getText());
        URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);
        for (URLSpan span : spans) {
            URLSpan newSpan = new URLSpan(span.getURL()) {
                @Override
                public void onClick(View widget) {
                    Links.open(widget.getContext(), getURL());
                }
            };
            text.setSpan(newSpan, text.getSpanStart(span), text.getSpanEnd(span), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            text.removeSpan(span);
        }
        textView.setText(text);
    }

    private ClickableSpan selectedLink = null;

    private NiceLinkMovementMethod() {
    }

    @Override
    public boolean onTouchEvent(final TextView widget, Spannable spannable, MotionEvent event) {
        boolean ret = false;
        int action = event.getAction();

        // Ripple Effects
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            MarkdownImageSpan[] images = spannable.getSpans(0, spannable.length(), MarkdownImageSpan.class);
            MarkdownImageSpan selectedImage = findSpanUnderTouch(widget, spannable, event, MarkdownImageSpan.class);
            for (MarkdownImageSpan image : images) {
                // Update Hotspot
                int hotspotX = (int) event.getX();
                int hotspotY = (int) event.getY();
                hotspotX -= widget.getTotalPaddingLeft();
                hotspotY -= widget.getTotalPaddingTop();
                image.setHotspot(hotspotX, hotspotY);

                // Enable/Disable Ripple
                if (image.getDrawable() instanceof RippleDrawable) {
                    int[] rippleState;
                    if (image == selectedImage) {
                        // Selected
                        if (action == MotionEvent.ACTION_DOWN) {
                            // Started Pressing On Drawable, Enable Ripple
                            rippleState = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
                        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                            // Stopped Pressing On Drawable, Disable Ripple
                            rippleState = new int[]{};
                        } else {
                            // Currently Pressing On Drawable, Don't Change State
                            rippleState = null;
                        }
                        // Consume Event
                        ret = true;
                    } else {
                        // Not Selected, Disable Ripple
                        rippleState = new int[]{};
                    }
                    // Update Ripple State
                    if (rippleState != null) {
                        image.getDrawable().setState(rippleState);
                    }
                }
            }
        }

        // Handle Event
        if (action == MotionEvent.ACTION_CANCEL) {
            // De-Select Link
            selectedLink = null;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            ClickableSpan link = findClickableSpanUnderTouch(widget, spannable, event);
            if (action == MotionEvent.ACTION_DOWN) {
                // Select Link
                selectedLink = link;
            } else if (link != null && link == selectedLink) {
                // Click Link
                link.onClick(widget);
            }

            // Consume Event If Link Is Selected
            if (selectedLink != null) {
                ret = true;
            }

            // De-Select Link On ACTION_UP
            if (action == MotionEvent.ACTION_UP) {
                selectedLink = null;
            }

            // Special Handling For Markdown Tables
            if (action == MotionEvent.ACTION_DOWN && findSpanUnderTouch(widget, spannable, event, TableRowSpan.class) != null) {
                // Consume Event
                ret = true;
            }
        }

        // Return
        return ret;
    }

    // https://github.com/saket/Better-Link-Movement-Method/blob/64be11b73db868a4fa615b53c0a76174227a7585/better-link-movement-method/src/main/java/me/saket/bettermovementmethod/BetterLinkMovementMethod.java#L294-L332
    private <T> T findSpanUnderTouch(TextView textView, Spannable text, MotionEvent event, Class<T> type) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        touchX -= textView.getTotalPaddingLeft();
        touchY -= textView.getTotalPaddingTop();

        final Layout layout = textView.getLayout();
        final int touchedLine = layout.getLineForVertical(touchY);
        final int touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX);

        final RectF touchedLineBounds = new RectF();
        touchedLineBounds.left = layout.getLineLeft(touchedLine);
        touchedLineBounds.top = layout.getLineTop(touchedLine);
        touchedLineBounds.right = layout.getLineWidth(touchedLine) + touchedLineBounds.left;
        touchedLineBounds.bottom = layout.getLineBottom(touchedLine);

        if (touchedLineBounds.contains(touchX, touchY)) {
            final Object[] spans = text.getSpans(touchOffset, touchOffset, type);
            for (final Object span : spans) {
                if (type.isInstance(span)) {
                    //noinspection unchecked
                    return (T) span;
                }
            }
        }
        return null;
    }
    private ClickableSpan findClickableSpanUnderTouch(TextView textView, Spannable text, MotionEvent event) {
        return findSpanUnderTouch(textView, text, event, ClickableSpan.class);
    }
}
