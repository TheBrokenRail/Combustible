package com.thebrokenrail.combustible.util.markdown.image;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.RequestManager;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.ViewImageActivity;
import com.thebrokenrail.combustible.util.Images;
import com.thebrokenrail.combustible.util.glide.GlideApp;
import com.thebrokenrail.combustible.util.glide.GlideUtil;

import org.commonmark.node.Image;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.spans.LinkSpan;
import io.noties.markwon.image.ImageProps;
import io.noties.markwon.image.ImageSize;

/**
 * Custom Markwon image plugin using Glide.
 */
public class MarkdownImagePlugin extends AbstractMarkwonPlugin {
    private final Context context;
    private final RequestManager requestManager;
    private final int cornerRadius;

    public MarkdownImagePlugin(Context context) {
        this.context = context;
        requestManager = GlideApp.with(context);
        cornerRadius = Images.getCornerRadius(context);
    }

    static Drawable getPlaceholder(Context context) {
        // Get Drawable
        Drawable placeholder = ContextCompat.getDrawable(context, R.drawable.baseline_image_24);
        assert placeholder != null;

        // Tint Drawable
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        @ColorInt int color = ContextCompat.getColor(context, typedValue.resourceId);
        DrawableCompat.setTint(DrawableCompat.wrap(placeholder).mutate(), color);

        // Return
        return placeholder;
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        // Add ImageSpans
        builder.setFactory(Image.class, (configuration, props) -> {
            String url = ImageProps.DESTINATION.require(props);
            ImageSize size = ImageProps.IMAGE_SIZE.get(props);
            return new MarkdownImageSpan(getPlaceholder(context), size, url);
        });

        // Clickable Images
        builder.appendFactory(Image.class, (configuration, props) -> {
            String url = ImageProps.DESTINATION.require(props);
            return new LinkSpan(configuration.theme(), url, (view, link) -> {
                Context context = view.getContext();
                Intent intent = new Intent(context, ViewImageActivity.class);
                intent.putExtra(ViewImageActivity.IMAGE_URL_EXTRA, link);
                context.startActivity(intent);
            });
        });
    }

    @Override
    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
        // Clear Images
        CharSequence text = textView.getText();
        if (text instanceof Spanned) {
            MarkdownImageSpan[] spans = ((Spanned) text).getSpans(0, text.length(), MarkdownImageSpan.class);
            for (MarkdownImageSpan span : spans) {
                // Remove Callback
                span.removeCallback();
                // Clear Target
                if (span.getTarget() != null) {
                    requestManager.clear(span.getTarget());
                }
            }
        }
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        // Check Settings
        boolean disableMarkdownImages = PreferenceManager.getDefaultSharedPreferences(textView.getContext()).getBoolean("disable_markdown_images", textView.getResources().getBoolean(R.bool.app_settings_disable_markdown_images_default));
        if (disableMarkdownImages) {
            return;
        }

        // Load Images
        CharSequence text = textView.getText();
        if (text instanceof Spanned) {
            MarkdownImageSpan[] spans = ((Spanned) text).getSpans(0, text.length(), MarkdownImageSpan.class);
            for (MarkdownImageSpan span : spans) {
                // Create Target
                span.attach(textView);
                // Wait Until Layout
                textView.post(() -> {
                    // Load Image
                    GlideUtil.load(textView.getContext(), requestManager, span.url, new ImageScaling(span.size, textView), cornerRadius, false, false, null, span.getTarget());
                });
            }
        }
    }
}
