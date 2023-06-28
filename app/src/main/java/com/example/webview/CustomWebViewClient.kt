package com.example.webview

import android.R
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.*
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.core.content.ContextCompat


class CustomWebViewClient(activity: Activity?) : WebChromeClient() {
    private var mActivity: Activity? = null
    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null
    private var mOriginalOrientation = 0
    private var mFullscreenContainer: FrameLayout? = null

    init {
        mActivity = activity
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (mCustomView != null) {
                callback.onCustomViewHidden()
                return
            }
            mOriginalOrientation = mActivity!!.requestedOrientation
            val decor = mActivity!!.window.decorView as FrameLayout
            mFullscreenContainer = FullscreenHolder(mActivity)
            (mFullscreenContainer as FullscreenHolder).addView(view, COVER_SCREEN_PARAMS)
            decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS)
            mCustomView = view
            setFullscreen(true)
            mCustomViewCallback = callback
        }
        super.onShowCustomView(view, callback)
    }

    override fun onShowCustomView(
        view: View?,
        requestedOrientation: Int,
        callback: CustomViewCallback
    ) {
        this.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        if (mCustomView == null) {
            return
        }
        setFullscreen(false)
        val decor = mActivity!!.window.decorView as FrameLayout
        decor.removeView(mFullscreenContainer)
        mFullscreenContainer = null
        mCustomView = null
        mCustomViewCallback!!.onCustomViewHidden()
        mActivity!!.requestedOrientation = mOriginalOrientation
    }

    private fun setFullscreen(enabled: Boolean) {
        val win: Window = mActivity!!.window
        val winParams: WindowManager.LayoutParams = win.getAttributes()
        val bits = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (enabled) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
            mCustomView?.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE)
        }
        win.setAttributes(winParams)
    }

    private class FullscreenHolder(ctx: Context?) : FrameLayout(ctx!!) {
        init {
            ctx?.let { ContextCompat.getColor(it, R.color.black) }?.let { setBackgroundColor(it) }
        }

        override fun onTouchEvent(evt: MotionEvent): Boolean {
            return true
        }
    }

    companion object {
        private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}
