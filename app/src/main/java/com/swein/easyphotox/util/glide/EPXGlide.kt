package com.swein.easyphotox.util.glide

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions

object EPXGlide {

    fun setImage(uri: Uri?, imageView: ImageView,
                 width: Int = 0, height: Int = 0, rate: Float = 0f, thumbnailSize: Float = 0f,
                 placeHolder: Int = -1, animation: Boolean = false) {

        var w = width
        var h = height
        var requestBuilder = Glide.with(imageView.context).asBitmap().load(uri)

        if (animation) {
            requestBuilder = requestBuilder.transition(
                BitmapTransitionOptions.withCrossFade()
            )
        }

        if (placeHolder != -1) {
            requestBuilder = requestBuilder.placeholder(placeHolder)
        }
        if (w != 0 && h != 0) {
            if (rate != 0f) {
                w = (w.toFloat() * rate).toInt()
                h = (h.toFloat() * rate).toInt()
            }
            requestBuilder = requestBuilder.override(w, h)
        }
        if (thumbnailSize != 0f) {
            requestBuilder = requestBuilder.thumbnail(thumbnailSize)
        }
        requestBuilder.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView)

    }
}