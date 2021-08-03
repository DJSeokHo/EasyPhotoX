package com.swein.easyphotox.resultprocessor

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.*

object SHCameraPhotoResultProcessor {
    
    fun uriListToCacheFilePathList(context: Context, imageUriList: MutableList<Uri>): MutableList<String> {

        val list = mutableListOf<String>()

        for (i in 0 until imageUriList.size) {
            list.add(fileFromContentUri(context, i, imageUriList[i]).absolutePath)
        }

        return list
    }

    private fun fileFromContentUri(context: Context, index: Int, contentUri: Uri): File {

        val fileExtension = getFileExtension(context, contentUri)
        val fileName = "cache_${context.packageName.replace(".", "_")}_$index.$fileExtension"

        val tempFile = File(context.cacheDir, fileName)
        if (tempFile.exists()) {
            tempFile.delete()
        }

        tempFile.createNewFile()

        val fileOutputStream = FileOutputStream(tempFile, false)
        val inputStream = context.contentResolver.openInputStream(contentUri)

        inputStream?.copyTo(fileOutputStream, DEFAULT_BUFFER_SIZE)

        fileOutputStream.flush()

        return tempFile
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        val fileType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType) ?: ""
    }
}