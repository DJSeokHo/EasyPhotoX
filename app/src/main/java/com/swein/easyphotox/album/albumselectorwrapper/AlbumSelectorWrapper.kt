package com.swein.easyphotox.album.albumselectorwrapper

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.swein.easyphotox.album.albumselectorwrapper.bean.AlbumSelectorItemBean
import com.swein.easyphotox.util.log.ILog
import java.text.SimpleDateFormat
import java.util.*

object AlbumSelectorWrapper {

    private const val TAG = "AlbumSelectorWrapper"

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun scanMediaFile(context: Context, offset: Int, limit: Int,
                      onSuccess: (albumSelectorItemBeanList: MutableList<AlbumSelectorItemBean>) -> Unit, onError: () -> Unit) {
        val albumSelectorItemBeanList: MutableList<AlbumSelectorItemBean> = mutableListOf()
        ILog.debug(TAG, "scanMediaFile")
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            // storage error
            onError()
            return
        }

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentResolver = context.contentResolver

        val projection = arrayOf(

            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.TITLE
        )

        val selection = "${MediaStore.Images.Media.MIME_TYPE} = ? or ${MediaStore.Images.Media.MIME_TYPE} = ? or ${MediaStore.Images.Media.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("image/jpeg", "image/jpg", "image/png")
        ILog.debug("??", "start")
            contentResolver.query(

            uri, projection,
            Bundle().apply {

                // Limit & Offset
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)

                // Sort function
                // columns must be a array !!!
                putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media.DATE_MODIFIED))
                putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)

                // Selection
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            }, null

        )?.use { cursor ->
            ILog.debug(TAG, "cursor ${cursor.count}")
            while (cursor.moveToNext()) {
                /*
                The number you are getting from your cursor is in the format of Unix Time which counts the number of seconds elapsed since 1 January 1970.
                For your date creation you want value in milliseconds. The easy solution is to multiply your result by 1000 and you are good to go.
                 */
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                val dateAddedLong = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)) * 1000
                val dateAdded = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateAddedLong))
                val dateModifiedLong = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)) * 1000
                val dateModified = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateModifiedLong))
                val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE))

                ILog.debug(TAG, "size is $size")
                ILog.debug(TAG, "dateAdded is $dateAdded")
                ILog.debug(TAG, "dateModified is $dateModified")
                ILog.debug(TAG, "displayName is $displayName")
                ILog.debug(TAG, "title is $title")

                val imageUrl = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                val albumSelectorItemBean  = AlbumSelectorItemBean()

                albumSelectorItemBean.imageUri = imageUrl
                albumSelectorItemBeanList.add(albumSelectorItemBean)
            }

            cursor.close()

            onSuccess(albumSelectorItemBeanList)

        }
    }


    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    fun scanFile(context: Context, offset: Int, limit: Int,
                 onSuccess: (albumSelectorItemBeanList: MutableList<AlbumSelectorItemBean>) -> Unit, onError: () -> Unit) {
        val albumSelectorItemBeanList: MutableList<AlbumSelectorItemBean> = mutableListOf()

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            // storage error
            onError()
            return
        }

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.TITLE
        )

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? or ${MediaStore.Files.FileColumns.MIME_TYPE} = ? or ${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("image/jpeg", "image/jpg", "image/png")

        contentResolver.query(

            uri, projection,
            Bundle().apply {

                // Limit & Offset
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)

                // Sort function
                // columns must be a array !!!
                putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED))
                putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)

                // Selection
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            }, null

        )?.use { cursor ->

            while (cursor.moveToNext()) {
                /*
                The number you are getting from your cursor is in the format of Unix Time which counts the number of seconds elapsed since 1 January 1970.
                For your date creation you want value in milliseconds. The easy solution is to multiply your result by 1000 and you are good to go.
                 */
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                val dateAddedLong = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000
                val dateAdded = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateAddedLong))
                val dateModifiedLong = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)) * 1000
                val dateModified = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateModifiedLong))
                val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE))

                ILog.debug(TAG, "size is $size")
                ILog.debug(TAG, "dateAdded is $dateAdded")
                ILog.debug(TAG, "dateModified is $dateModified")
                ILog.debug(TAG, "displayName is $displayName")
                ILog.debug(TAG, "title is $title")

                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                ILog.debug(TAG, "imageUri is $imageUri")

                val albumSelectorItemBean  = AlbumSelectorItemBean()

                albumSelectorItemBean.imageUri = imageUri
                albumSelectorItemBeanList.add(albumSelectorItemBean)
            }

            cursor.close()

            onSuccess(albumSelectorItemBeanList)

        }
    }


    @SuppressLint("SimpleDateFormat")
    fun scanFile(context: Context, onSuccess: (albumSelectorItemBeanList: MutableList<AlbumSelectorItemBean>) -> Unit, onError: () -> Unit) {
        val albumSelectorItemBeanList: MutableList<AlbumSelectorItemBean> = mutableListOf()

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            // storage error
            onError()
            return
        }

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.TITLE
        )

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? or ${MediaStore.Files.FileColumns.MIME_TYPE} = ? or ${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("image/jpeg", "image/jpg", "image/png")

        contentResolver.query(
            uri, projection, selection, selectionArgs, "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->

            while (cursor.moveToNext()) {
                /*
                The number you are getting from your cursor is in the format of Unix Time which counts the number of seconds elapsed since 1 January 1970.
                For your date creation you want value in milliseconds. The easy solution is to multiply your result by 1000 and you are good to go.
                 */
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                val dateAddedLong = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000
                val dateAdded = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateAddedLong))
                val dateModifiedLong = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)) * 1000
                val dateModified = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateModifiedLong))
                val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE))

                ILog.debug(TAG, "size is $size")
                ILog.debug(TAG, "dateAdded is $dateAdded")
                ILog.debug(TAG, "dateModified is $dateModified")
                ILog.debug(TAG, "displayName is $displayName")
                ILog.debug(TAG, "title is $title")

                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                ILog.debug(TAG, "imageUri is $imageUri")

                val albumSelectorItemBean  = AlbumSelectorItemBean()

                albumSelectorItemBean.imageUri = imageUri
                albumSelectorItemBeanList.add(albumSelectorItemBean)
            }

            cursor.close()

            onSuccess(albumSelectorItemBeanList)

        }
    }
}