package com.thebrokenrail.combustible.util.markdown;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.ViewImageActivity;
import com.thebrokenrail.combustible.util.Images;
import com.thebrokenrail.combustible.util.glide.GlideApp;
import com.thebrokenrail.combustible.util.glide.GlideUtil;

import org.commonmark.node.Image;

import java.util.HashMap;
import java.util.Map;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.spans.LinkSpan;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.AsyncDrawableLoader;
import io.noties.markwon.image.AsyncDrawableScheduler;
import io.noties.markwon.image.DrawableUtils;
import io.noties.markwon.image.ImageProps;
import io.noties.markwon.image.ImageSpanFactory;

/**
 * <a href="https://github.com/noties/Markwon/blob/2ea148c30a07f91ffa37c0aa36af1cf2670441af/markwon-image-glide/src/main/java/io/noties/markwon/image/glide/GlideImagesPlugin.java">GlideImagesPlugin</a> with some customizations.
 */
public class CustomImagePlugin extends AbstractMarkwonPlugin {
    private final GlideAsyncDrawableLoader glideAsyncDrawableLoader;

    public CustomImagePlugin(Context context) {
        this.glideAsyncDrawableLoader = new GlideAsyncDrawableLoader(GlideApp.with(context), context);
    }

    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        builder.setFactory(Image.class, new ImageSpanFactory());

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
    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
        builder.asyncDrawableLoader(glideAsyncDrawableLoader);
    }

    @Override
    public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
        AsyncDrawableScheduler.unschedule(textView);
    }

    @Override
    public void afterSetText(@NonNull TextView textView) {
        AsyncDrawableScheduler.schedule(textView);
    }

    private static class GlideAsyncDrawableLoader extends AsyncDrawableLoader {
        private final RequestManager requestManager;
        private final Context context;
        private final int cornerRadius;
        private final Map<AsyncDrawable, Target<?>> cache = new HashMap<>(2);

        private GlideAsyncDrawableLoader(@NonNull RequestManager requestManager, Context context) {
            this.requestManager = requestManager;
            this.context = context;
            cornerRadius = Images.getCornerRadius(context);
        }

        @Override
        public void load(@NonNull AsyncDrawable drawable) {
            AsyncDrawableTarget target = new AsyncDrawableTarget(drawable);
            cache.put(drawable, target);
            GlideUtil.load(requestManager, drawable.getDestination(), null, cornerRadius, false, false, null, target);
        }

        @Override
        public void cancel(@NonNull AsyncDrawable drawable) {
            final Target<?> target = cache.remove(drawable);
            if (target != null) {
                requestManager.clear(target);
            }
        }

        @Nullable
        @Override
        public Drawable placeholder(@NonNull AsyncDrawable drawable) {
            return ContextCompat.getDrawable(context, R.drawable.baseline_image_24);
        }

        private class AsyncDrawableTarget extends CustomTarget<Drawable> {
            private final AsyncDrawable drawable;
            @Nullable
            private Animatable animatable = null;

            private AsyncDrawableTarget(@NonNull AsyncDrawable drawable) {
                this.drawable = drawable;
            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                if (cache.remove(drawable) != null) {
                    if (drawable.isAttached()) {
                        setResourceInternal(resource);
                    }
                }
            }

            @Override
            public void onLoadFailed(@Nullable Drawable unused) {
                cache.remove(drawable);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable unused) {
                if (animatable != null) {
                    animatable.stop();
                }
                drawable.clearResult();
                if (drawable.isAttached()) {
                    Drawable placeholderDrawable = placeholder(drawable);
                    assert placeholderDrawable != null;
                    setResourceInternal(placeholderDrawable);
                }
            }

            @Override
            public void onStart() {
                if (animatable != null) {
                    animatable.start();
                }
            }

            @Override
            public void onStop() {
                if (animatable != null) {
                    animatable.stop();
                }
            }

            private void setResourceInternal(Drawable resource) {
                DrawableUtils.applyIntrinsicBoundsIfEmpty(resource);
                drawable.setResult(resource);
                maybeUpdateAnimatable(resource);
            }

            private void maybeUpdateAnimatable(Drawable resource) {
                if (resource instanceof Animatable) {
                    animatable = (Animatable) resource;
                    animatable.start();
                } else {
                    animatable = null;
                }
            }
        }
    }
}
