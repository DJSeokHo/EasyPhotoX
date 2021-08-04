package com.swein.easyphotox.album.selector.adapter.item

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.swein.easyphotox.R
import com.swein.easyphotox.album.albumselectorwrapper.bean.AlbumSelectorItemBean
import com.swein.easyphotox.util.glide.SHGlide
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

    // flag
    var enableClick = true
    // flag

    var index = -1

    init {

        findView()
        setListener()
        initView()
    }

    private fun findView() {

        view.get()?.let {
            imageView = it.findViewById(R.id.imageView)
            imageViewCheck = it.findViewById(R.id.imageViewCheck)
        }
    }

    private fun setListener() {

        imageViewCheck.setOnClickListener {
            if (!albumSelectorItemBean.isSelected && !enableClick) {
                return@setOnClickListener
            }
            albumSelectorItemBean.isSelected = !albumSelectorItemBean.isSelected
            toggleCheck()
        }

        imageView.setOnClickListener {
            if (!albumSelectorItemBean.isSelected && !enableClick) {
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

        toggleCheck()

        imageView.post {
            SHGlide.setImage(albumSelectorItemBean.imageUri, imageView, imageView.width, imageView.height, 0.7f)
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

}