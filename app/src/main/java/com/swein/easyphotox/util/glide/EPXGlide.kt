package com.swein.easyphotox.util.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.io.File

object EPXGlide {

    fun setImage(
        imageView: ImageView,
        width: Int = 0,
        height: Int = 0,
        rate: Float = 0f,
        thumbnailSize: Float = 0f,
        isAnimation: Boolean = false,
        isCircle: Boolean = false,
        uri: Uri? = null,
        url: String? = null,
        filePath: String? = null,
        bitmap: Bitmap? = null,
        file: File? = null,
        placeHolder: Drawable? = null
    ) {

        var requestBuilder = Glide.with(imageView.context).asBitmap()

        uri?.let {
            requestBuilder = requestBuilder.load(it)
        }

        url?.let {
            requestBuilder = requestBuilder.load(it)
        }

        filePath?.let {
            requestBuilder = requestBuilder.load(it)
        }

        bitmap?.let {
            requestBuilder = requestBuilder.load(it)
        }

        file?.let {
            requestBuilder = requestBuilder.load(it)
        }

        if (isAnimation) {
            requestBuilder = requestBuilder.transition(BitmapTransitionOptions.withCrossFade())
        }

        if (placeHolder != null) {
            requestBuilder = requestBuilder.placeholder(placeHolder)
        }
        if (width != 0 && height != 0) {

            var w = width
            var h = height
            if (rate != 0f) {
                w = (width.toFloat() * rate).toInt()
                h = (height.toFloat() * rate).toInt()
            }
            requestBuilder = requestBuilder.override(w, h)
        }
        if (thumbnailSize != 0f) {
            requestBuilder = requestBuilder.thumbnail(thumbnailSize)
        }

        val options = requestBuilder.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)

        if (isCircle) {
            requestBuilder = options.apply(RequestOptions.circleCropTransform())
        }

        requestBuilder.into(imageView)
    }
}