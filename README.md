# EasyPhotoX
easy photo base on cameraX

# step 1: Add it in your root build.gradle at the end of repositories:

```
allprojects {

  repositories {
  
    ...
    maven { url 'https://jitpack.io' }
    
  }
  
}
```

# step 2: Add the dependency

```
dependencies {

  implementation 'com.github.DJSeokHo:EasyPhotoX:1.0.14'
  
}
```

# How to use

for example, if you want to open camera in your activity, so the XML should be like this:
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".EasyPhotoXDemoActivity"
    android:background="@color/black"
    android:id="@+id/container">

</FrameLayout>
```

and your activity should be like this:
```
class EasyPhotoXDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_easy_photo_x)

        EasyPhotoXFragment.startFragment(activity = this,
            fragmentContainer =  R.id.container,
            imageLimit = 10, // how many photo can take with camera and selected from album, the maximum
            permissionDialogTitle = "title of dialog when need to go to app setting screen to request permission",
            permissionDialogMessage = "message of dialog when need to go to app setting screen to request permission",
            permissionDialogPositiveButtonTitle = "positive button title of dialog when need to go to app setting screen to request permission",
            onImageSelected = { imageFilePathList -> // image path list that take with camera and selected from album

                // take photo or select image done
                EPXLog.debug("???", "${imageFilePathList.size}")

                // remove photo fragment
                EasyPhotoXFragment.destroyFragment(this)

        }, onCloseCamera = {

                // remove photo fragment
                EasyPhotoXFragment.destroyFragment(this)
        })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        EasyPermissionManager.onActivityResult(requestCode, resultCode)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {

        if (EasyPhotoXFragment.destroyFragment(this)) {
            return
        }

        super.onDestroy()
    }
}
```
