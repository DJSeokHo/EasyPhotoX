package com.swein.easyphotox.shselectedimageviewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swein.easyphotox.R
import com.swein.easyphotox.shselectedimageviewholder.adapter.SHSelectedImageViewHolderAdapter
import com.swein.easyphotox.shselectedimageviewholder.adapter.item.ImageSelectedItemBean

class SHSelectedImageViewHolder (
    context: Context?,
    private var delegate: SHSelectedImageViewHolderDelegate?,
    private var imageSelectedItemBeanList: MutableList<ImageSelectedItemBean> = mutableListOf()
) {

    companion object {
        const val TAG = "SHSelectedImageViewHolder"
    }

    interface SHSelectedImageViewHolderDelegate {
        fun onDelete(imageSelectedItemBean: ImageSelectedItemBean)
        fun onClose()
    }

    var view: View = LayoutInflater.from(context).inflate(R.layout.view_holder_sh_selected_image, null)

    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: SHSelectedImageViewHolderAdapter
    private lateinit var imageButtonClose: ImageButton

    init {
        findView()
        setListener()
        initList()
    }

    private fun findView() {

        recyclerView = view.findViewById(R.id.recyclerView)
        imageButtonClose = view.findViewById(R.id.imageButtonClose)

    }

    private fun setListener() {
        imageButtonClose.setOnClickListener {
            delegate?.onClose()
        }

    }

    private fun initList() {

        linearLayoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)

        adapter = SHSelectedImageViewHolderAdapter(object : SHSelectedImageViewHolderAdapter.SHSelectedImageViewHolderAdapterDelegate {
            override fun onDelete(imageSelectedItemBean: ImageSelectedItemBean) {

                adapter.delete(imageSelectedItemBean)
                delegate?.onDelete(imageSelectedItemBean)

            }
        })

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

        reload()
    }

    private fun reload() {
        adapter.reload(imageSelectedItemBeanList)
    }

    fun insert(imageSelectedItemBean: ImageSelectedItemBean) {
        adapter.insert(imageSelectedItemBean)
        recyclerView.smoothScrollToPosition(0)
    }

}