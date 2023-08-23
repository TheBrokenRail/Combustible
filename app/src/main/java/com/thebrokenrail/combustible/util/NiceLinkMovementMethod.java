package com.thebrokenrail.combustible.util;

import android.graphics.RectF;
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

        if (action == MotionEvent.ACTION_CANCEL) {
            selectedLink = null;
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            ClickableSpan link = findClickableSpanUnderTouch(widget, spannable, event);

            if (action == MotionEvent.ACTION_DOWN) {
                selectedLink = link;
            } else if (link != null && link == selectedLink) {
                link.onClick(widget);
            }

            ret = selectedLink != null;

            if (action == MotionEvent.ACTION_UP) {
                selectedLink = null;
            }
        }

        return ret;
    }

    // https://github.com/saket/Better-Link-Movement-Method/blob/64be11b73db868a4fa615b53c0a76174227a7585/better-link-movement-method/src/main/java/me/saket/bettermovementmethod/BetterLinkMovementMethod.java#L294-L332
    private ClickableSpan findClickableSpanUnderTouch(TextView textView, Spannable text, MotionEvent event) {
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
            final Object[] spans = text.getSpans(touchOffset, touchOffset, ClickableSpan.class);
            for (final Object span : spans) {
                if (span instanceof ClickableSpan) {
                    return (ClickableSpan) span;
                }
            }
        }
        return null;
    }
}
