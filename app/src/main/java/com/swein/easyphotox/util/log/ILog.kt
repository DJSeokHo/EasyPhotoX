package com.swein.easyphotox.util.log

import android.util.Log
import androidx.camera.extensions.BuildConfig

class ILog {

    companion object {

        private const val TAG: String = "ILog ======> "

        fun debug(tag: String, content: Any?) {

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "$tag $content")
            }
        }
    }

}