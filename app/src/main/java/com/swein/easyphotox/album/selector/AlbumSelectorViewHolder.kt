package com.swein.easyphotox.album.selector

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.swein.easyphotox.album.albumselectorwrapper.AlbumSelectorWrapper
import com.swein.easyphotox.album.albumselectorwrapper.bean.AlbumSelectorItemBean
import com.swein.easyphotox.album.selector.adapter.AlbumSelectorAdapter
import com.swein.easyphotox.util.log.ILog
import com.swein.easyphotox.util.thread.ThreadUtility
import com.swein.easyphotox.R
import com.swein.easyphotox.util.eventsplitshot.eventcenter.EventCenter
import com.swein.easyphotox.util.eventsplitshot.subject.ESSArrows

class AlbumSelectorViewHolder(
    context: Context,
    private val maxSelect: Int,
    private val delegate: AlbumSelectorViewHolderDelegate
) {

    companion object {
        private const val TAG = "AlbumSelectorViewHolder"

    }

    interface AlbumSelectorViewHolderDelegate {
        fun onConfirm()
        fun onClose()
    }

    val view: View = LayoutInflater.from(context).inflate(R.layout.view_holder_album_selector, null)

    private lateinit var albumSelectorAdapter: AlbumSelectorAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var textViewAction: TextView
    private lateinit var textViewSelected: TextView

    private var selectedList: MutableList<AlbumSelectorItemBean> = mutableListOf()

    init {
        findView()
        setListener()
        initList()

        reload()
    }

    private fun findView() {
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        textViewAction = view.findViewById(R.id.textViewAction)
        textViewSelected = view.findViewById(R.id.textViewSelected)
    }

    private fun setListener() {
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            reload()
        }

        textViewAction.setOnClickListener {

            if(selectedList.isEmpty()) {
                delegate.onClose()
            }
            else {
                delegate.onConfirm()
            }
        }
    }

    private fun initList() {
        layoutManager = GridLayoutManager(view.context, 3)
        albumSelectorAdapter = AlbumSelectorAdapter(object :
            AlbumSelectorAdapter.AlbumSelectorAdapterDelegate {
            override fun onLoadMore() {
                loadMore()
            }

            override fun onSelected() {
                selectedList.clear()

                for (i in albumSelectorAdapter.albumSelectorItemBeanList.indices) {
                    if (albumSelectorAdapter.albumSelectorItemBeanList[i].isSelected) {
                        selectedList.add(albumSelectorAdapter.albumSelectorItemBeanList[i])
                    }
                }

                for (i in selectedList.indices) {
                    ILog.debug(TAG, "${selectedList[i].imageUri.path} ${selectedList[i].isSelected}")
                }

                if (selectedList.size >= maxSelect) {
                    EventCenter.sendEvent(
                        ESSArrows.DISABLE_LIST_ITEM_CLICK,
                        this,
                        null
                    )
                }
                else {
                    EventCenter.sendEvent(ESSArrows.ENABLE_LIST_ITEM_CLICK, this, null)
                }

                textViewAction.text = if (getSelectedImagePath().isEmpty()) {
                    view.context.getString(R.string.camera_cancel)
                }
                else {
                    view.context.getString(R.string.camera_confirm)
                }

                textViewSelected.text = String.format(view.context.getString(R.string.selected_image_count), getSelectedImagePath().size.toString(), maxSelect.toString())
            }

        })

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = albumSelectorAdapter
    }

    private fun reload() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            ThreadUtility.startThread {

                AlbumSelectorWrapper.scanMediaFile(view.context, 0, 50, { albumSelectorItemBeanList ->
//                AlbumSelectorWrapper.scanFile(view.context, { albumSelectorItemBeanList ->

                    ThreadUtility.startUIThread(0) {

                        albumSelectorAdapter.reload(albumSelectorItemBeanList)
                    }

                }, {
                    ThreadUtility.startUIThread(0) {
                        ILog.debug(TAG, "error")
                    }
                })
            }
        }
    }

    private fun loadMore() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            ThreadUtility.startThread {

                AlbumSelectorWrapper.scanMediaFile(view.context, albumSelectorAdapter.itemCount, 50, { albumSelectorItemBeanList ->

                    ThreadUtility.startUIThread(0) {

                        albumSelectorAdapter.loadMore(albumSelectorItemBeanList)
                    }

                }, {
                    ThreadUtility.startUIThread(0) {
                        ILog.debug(TAG, "error")
                    }
                })
            }
        }
    }

    fun getSelectedImagePath(): MutableList<Uri> {
        val list = mutableListOf<Uri>()

        for (i in 0 until selectedList.size) {
            list.add(selectedList[i].imageUri)
        }
        return list
    }

}