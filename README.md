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

  implementation 'com.github.DJSeokHo:EasyPhotoX:1.0.15'
  
}
```

# How to use

first of all, don't forget permissions in your manifest:
```
<uses-feature
    android:name="android.hardware.camera"
    android:required="true" />
<uses-feature
    android:name="android.hardware.camera.autofocus"
    android:required="false" />

<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    tools:ignore="ScopedStorage" />

<application
    ...
    ...

    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${applicationId}.provider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths" />
    </provider>

</application>
```

and add xml folder in your res folder, and create file_paths.xml in the folder:
```
res
 - xml
  - file_paths.xml
```

in your file_paths.xml:
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-media-path name="media" android:path="@{string/app_name}/" />
    <files-path name="files" path="."/>
</paths>
```

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
