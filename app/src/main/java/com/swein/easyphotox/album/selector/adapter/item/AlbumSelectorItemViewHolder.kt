package com.swein.easyphotox.album.selector.adapter.item

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.swein.easyeventobserver.EventCenter
import com.swein.easyphotox.R
import com.swein.easyphotox.album.albumselectorwrapper.bean.AlbumSelectorItemBean
import com.swein.easyphotox.constants.EPXConstants
import com.swein.easyphotox.util.glide.EPXGlide
import java.lang.ref.WeakReference

class AlbumSelectorItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    interface AlbumSelectorItemViewHolderDelegate {
        fun onSelected()
    }

    lateinit var albumSelectorItemBean: AlbumSelectorItemBean

    private var view: WeakReference<View> = WeakReference(itemView)

    private lateinit var imageView: ImageView
    private lateinit var imageViewCheck: ImageView

    var albumSelectorItemViewHolderDelegate: AlbumSelectorItemViewHolderDelegate? = null

    private var click = true

    init {

        findView()
        setListener()
        initView()
    }

    private fun initESS() {
        EventCenter.addEventObserver(
            EPXConstants.ENABLE_LIST_ITEM_CLICK, this, object : EventCenter.EventRunnable {
                override fun run(arrow: String, poster: Any, data: MutableMap<String, Any>?) {
                    click = true
                }
            })

        EventCenter.addEventObserver(
            EPXConstants.DISABLE_LIST_ITEM_CLICK, this, object : EventCenter.EventRunnable {
                override fun run(arrow: String, poster: Any, data: MutableMap<String, Any>?) {
                    click = false
                }
            })
    }

    private fun findView() {

        view.get()?.let {
            imageView = it.findViewById(R.id.imageView)
            imageViewCheck = it.findViewById(R.id.imageViewCheck)
        }
    }

    private fun setListener() {

        imageViewCheck.setOnClickListener {
            if (!albumSelectorItemBean.isSelected && !click) {
                return@setOnClickListener
            }
            albumSelectorItemBean.isSelected = !albumSelectorItemBean.isSelected
            toggleCheck()
        }

        imageView.setOnClickListener {
            if (!albumSelectorItemBean.isSelected && !click) {
                return@setOnClickListener
            }
            albumSelectorItemBean.isSelected = !albumSelectorItemBean.isSelected
            toggleCheck()
        }
    }

    private fun initView() {
        imageView.post {
            val layoutParams = imageView.layoutParams
            layoutParams.height = imageView.width
        }
    }

    fun updateView() {

        initESS()
        toggleCheck()

        imageView.post {
            EPXGlide.setImage(imageView, imageView.width, imageView.height,0.7f, uri = albumSelectorItemBean.imageUri)
        }
    }

    private fun toggleCheck() {
        albumSelectorItemViewHolderDelegate!!.onSelected()

        if (albumSelectorItemBean.isSelected) {
            imageViewCheck.setImageResource(R.mipmap.ti_check)
        }
        else {
            imageViewCheck.setImageResource(R.mipmap.ti_un_check)
        }
    }

    fun removeESS() {
        EventCenter.removeAllObserver(this)
    }

}