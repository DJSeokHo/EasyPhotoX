package com.swein.easyphotox.util.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.WindowManager

object EPXThemeUtility {

    /**
     * hide system top status bar and bottom navigation button bar
     */
    fun hideSystemUI(activity: Activity) {
        val decorView = activity.window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
    }

    /**
     * show system top status bar and bottom navigation button bar
     */
    fun showSystemUI(activity: Activity) {
        val decorView = activity.window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
    }

}