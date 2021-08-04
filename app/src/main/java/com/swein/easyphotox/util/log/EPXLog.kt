package com.swein.easyphotox.util.log

import android.util.Log
import androidx.camera.extensions.BuildConfig

class EPXLog {

    companion object {

        private const val TAG: String = "EPXLog ======> "

        fun debug(tag: String, content: Any?) {

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "$tag $content")
            }
        }
    }

}