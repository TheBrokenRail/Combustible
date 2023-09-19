package com.thebrokenrail.combustible.util.markdown.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

import io.noties.markwon.image.ImageSize;
import io.noties.markwon.image.ImageSizeResolverDef;

class ImageScaling extends BitmapTransformation {
    private static class CustomImageSizeResolver extends ImageSizeResolverDef {
        private final float textSize;
        private final int textViewWidth;

        private CustomImageSizeResolver(float textSize, int textViewWidth) {
            this.textSize = textSize;
            this.textViewWidth = textViewWidth;
        }

        private Rect resolveImageSize(Bitmap bitmap, ImageSize size) {
            // Calculate Size
            Rect newSize = resolveImageSize(size, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), textViewWidth, textSize);

            // Fit To Width
            if (newSize.width() > textViewWidth) {
                float reduceRatio = (float) newSize.width() / textViewWidth;
                newSize = new Rect(
                        0,
                        0,
                        textViewWidth,
                        (int) (newSize.height() / reduceRatio + 0.5f)
                );
            }

            // Return
            return newSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CustomImageSizeResolver that = (CustomImageSizeResolver) o;
            return Float.compare(that.textSize, textSize) == 0 && textViewWidth == that.textViewWidth;
        }

        @Override
        public int hashCode() {
            return Objects.hash(textSize, textViewWidth);
        }
    }

    private static final String ID = "com.thebrokenrail.combustible.util.markdown.image.MarkdownImageScaling";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    private static final Paint DEFAULT_PAINT = new Paint(TransformationUtils.PAINT_FLAGS);

    @NonNull
    private final CustomImageSizeResolver sizeResolver;
    @NonNull
    private final ImageSize size;

    ImageScaling(@Nullable ImageSize size, TextView textView) {
        float textSize = textView.getPaint().getTextSize();
        int textViewWidth = textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
        sizeResolver = new CustomImageSizeResolver(textSize, textViewWidth);
        if (size == null) {
            size = new ImageSize(null, null);
        }
        this.size = size;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        // Get Calculate New Size
        Rect newSize;
        boolean hasUserProvidedSize = size.width != null || size.height != null;
        while (true) {
            newSize = sizeResolver.resolveImageSize(toTransform, hasUserProvidedSize ? size : null);
            if (newSize.width() > 0 && newSize.height() > 0) {
                // Valid Size
                break;
            }
            if (hasUserProvidedSize) {
                // Try Without Custom Size
                hasUserProvidedSize = false;
            } else {
                // Give Up
                return toTransform;
            }
        }
        final float widthPercentage = newSize.width() / (float) toTransform.getWidth();
        final float heightPercentage = newSize.height() / (float) toTransform.getHeight();

        // Create New Bitmap (From TransformationUtils.fitCenter)
        Bitmap.Config config = toTransform.getConfig();
        Bitmap toReuse = pool.get(newSize.width(), newSize.height(), config);
        TransformationUtils.setAlpha(toTransform, toReuse);

        // Resize Bitmap
        Matrix matrix = new Matrix();
        matrix.setScale(widthPercentage, heightPercentage);
        Lock lock = TransformationUtils.getBitmapDrawableLock();
        lock.lock();
        try {
            Canvas canvas = new Canvas(toReuse);
            canvas.drawBitmap(toTransform, matrix, DEFAULT_PAINT);
            canvas.setBitmap(null);
        } finally {
            lock.unlock();
        }

        // Return
        return toReuse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageScaling that = (ImageScaling) o;
        return sizeResolver.equals(that.sizeResolver) && size.toString().equals(that.size.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, sizeResolver, size.toString());
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        messageDigest.update(size.toString().getBytes(CHARSET));
        byte[] data = ByteBuffer.allocate(8).putInt(sizeResolver.textViewWidth).putFloat(sizeResolver.textSize).array();
        messageDigest.update(data);
    }
}
