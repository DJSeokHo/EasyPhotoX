//package com.swein.easyphotox
//
//import android.Manifest
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
//        EasyPermissionManager.requestPermission(this,
//            9999,
//            "Permission",
//            "permissions are necessary",
//            "setting",
//            listOf(Manifest.permission.CAMERA,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        ) {
//            startCamera()
//        }
//    }
//
//    private fun startCamera() {
//
//        EasyPhotoXFragment.startFragment(this, R.id.container, 10, onImageSelected = {
//
//            EPXLog.debug("???", "${it.size}")
//
//            EasyPhotoXFragment.destroyFragment(this)
//        }, onCloseCamera = {
//            EasyPhotoXFragment.destroyFragment(this)
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