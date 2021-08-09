//package com.swein.easyphotox
//
//import android.content.Intent
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import com.swein.easypermissionmanager.EasyPermissionManager
//import com.swein.easyphotox.camera.EasyPhotoXFragment
//import com.swein.easyphotox.util.log.EPXLog
//
//class EasyPhotoXDemoActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_easy_photo_x)
//
//        EasyPhotoXFragment.startFragment(activity = this,
//            fragmentContainer =  R.id.container,
//            imageLimit = 10, // how many photo can take with camera and selected from album, the maximum
//            permissionDialogTitle = "title of dialog when need to go to app setting screen to request permission",
//            permissionDialogMessage = "message of dialog when need to go to app setting screen to request permission",
//            permissionDialogPositiveButtonTitle = "positive button title of dialog when need to go to app setting screen to request permission",
//            onImageSelected = { imageFilePathList -> // image path list that take with camera and selected from album
//
//                // take photo or select image done
//                EPXLog.debug("???", "${imageFilePathList.size}")
//
//                // remove photo fragment
//                EasyPhotoXFragment.destroyFragment(this)
//
//        }, onCloseCamera = {
//
//                // remove photo fragment
//                EasyPhotoXFragment.destroyFragment(this)
//        })
//
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        EasyPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        EasyPermissionManager.onActivityResult(requestCode, resultCode)
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//
//    override fun onDestroy() {
//
//        if (EasyPhotoXFragment.destroyFragment(this)) {
//            return
//        }
//
//        super.onDestroy()
//    }
//}