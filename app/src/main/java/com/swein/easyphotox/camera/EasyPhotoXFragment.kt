package com.swein.easyphotox.camera

import com.swein.easyphotox.R

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.common.util.concurrent.ListenableFuture
import com.swein.androidkotlintool.framework.module.shcameraphoto.camera.NormalImageRealTimeAnalyzer
import com.swein.easyphotox.album.selector.AlbumSelectorViewHolder
import com.swein.easyphotox.resultprocessor.SHCameraPhotoResultProcessor
import com.swein.easyphotox.shselectedimageviewholder.SHSelectedImageViewHolder
import com.swein.easyphotox.shselectedimageviewholder.adapter.item.ImageSelectedItemBean
import com.swein.easyphotox.util.date.DateUtility
import com.swein.easyphotox.util.eventsplitshot.eventcenter.EventCenter
import com.swein.easyphotox.util.eventsplitshot.subject.ESSArrows
import com.swein.easyphotox.util.log.ILog
import com.swein.easyphotox.util.sound.audiomanager.AudioManagerUtility
import com.swein.easyphotox.util.sound.mediaplayer.MediaPlayerUtility
import com.swein.easyphotox.util.theme.ThemeUtility
import com.swein.easyphotox.util.thread.ThreadUtility

import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class EasyPhotoXFragment : Fragment() {

    companion object {

        const val TAG = "EasyCameraPhotoFragment"
        private const val PHOTO_EXTENSION = ".jpg"

        @JvmStatic
        fun newInstance(limit: Int) =
            EasyPhotoXFragment().apply {
                arguments = Bundle().apply {
                    putInt("limit", limit)
                }
            }

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private lateinit var imageButtonTake: ImageButton
    private lateinit var imageButtonSwitchCamera: ImageButton
    private lateinit var imageButtonAlbum: ImageButton
    private lateinit var imageButtonFlash: ImageButton
    private lateinit var textViewAction: TextView
    private lateinit var imageView: ImageView
    private lateinit var textViewImageCount: TextView
    private lateinit var frameLayoutProgress: FrameLayout

    private lateinit var previewView: PreviewView

    private lateinit var preview: Preview
    private lateinit var camera: Camera
    private lateinit var cameraProvider: ProcessCameraProvider
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var imageCapture: ImageCapture
    private lateinit var imageAnalysis: ImageAnalysis
    private var flash = false

    private var limit = 0
    private var fromWhere = ""

    private var selectedImageList = mutableListOf<ImageSelectedItemBean>()
    private var shSelectedImageViewHolder: SHSelectedImageViewHolder? = null
    private lateinit var frameLayoutSelectedImageArea: FrameLayout

    private lateinit var frameLayoutRoot: FrameLayout
    private var albumSelectorViewHolder: AlbumSelectorViewHolder? = null

    private var fullScreenRatio: Float = 0f

    private val displayManager by lazy {
        requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }
    private var displayId: Int = -1

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@EasyPhotoXFragment.displayId) {
                ILog.debug(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture.targetRotation = view.display.rotation
                imageAnalysis.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    /**
     * add android:configChanges="keyboardHidden|orientation|screenSize"
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            // Nothing need to be done here
            ILog.debug(TAG, "ORIENTATION_LANDSCAPE")

        }
        else {
            ILog.debug(TAG, "ORIENTATION_PORTRAIT")
            // Nothing need to be done here
        }

        bindCameraUseCases()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkBundle()
        initData()
    }

    private fun initAudio(rootView: View) {

        AudioManagerUtility.init(rootView.context)
        MediaPlayerUtility.init(rootView.context, R.raw.camera_shutter_click) {
            AudioManagerUtility.resetAfterPlay()
        }

    }

    private fun checkBundle() {
        arguments?.let {
            limit = it.getInt("limit", 0)
            fromWhere = it.getString("fromWhere", "")
        }

        ILog.debug(TAG, "limit ?? $limit")
        if(limit == 0) {
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_easy_photo_x, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAudio(view)
        findView(view)
        setListener()

        initView()
        // Wait for the views to be properly laid out
        previewView.post {

            // Keep track of the display in which this view is attached
            displayId = previewView.display.displayId

            // Set up the camera and its use cases
            activity?.let {
                updateFlashImage()
                initCamera(it)
            }
        }
    }

    private fun initData() {

        selectedImageList.clear()
    }

    private fun initView() {
        textViewImageCount.text = "0"
    }

    private fun findView(rootView: View) {
        frameLayoutRoot = rootView.findViewById(R.id.frameLayoutRoot)

        imageButtonTake = rootView.findViewById(R.id.imageButtonTake)
        imageButtonSwitchCamera = rootView.findViewById(R.id.imageButtonSwitchCamera)
        textViewAction = rootView.findViewById(R.id.textViewAction)
        imageView = rootView.findViewById(R.id.imageView)
        previewView = rootView.findViewById(R.id.previewView)
        imageButtonFlash = rootView.findViewById(R.id.imageButtonFlash)
        textViewImageCount = rootView.findViewById(R.id.textViewImageCount)
        frameLayoutSelectedImageArea = rootView.findViewById(R.id.frameLayoutSelectedImageArea)
        imageButtonAlbum = rootView.findViewById(R.id.imageButtonAlbum)

        frameLayoutProgress = rootView.findViewById(R.id.frameLayoutProgress)
    }

    private fun setListener() {

        imageButtonAlbum.setOnClickListener {
            showAlbumSelector()
        }

        imageView.setOnClickListener {

            if (selectedImageList.isEmpty()) {
                return@setOnClickListener
            }

            showSelectedImageArea()
        }

        imageButtonFlash.setOnClickListener {

            flash = !flash

            imageCapture.flashMode = if(flash) {
                ImageCapture.FLASH_MODE_AUTO
            }
            else {
                ImageCapture.FLASH_MODE_OFF
            }

            updateFlashImage()
        }

        imageButtonSwitchCamera.setOnClickListener {

            lensFacing = if (CameraSelector.LENS_FACING_BACK == lensFacing) {
                CameraSelector.LENS_FACING_FRONT
            }
            else {
                CameraSelector.LENS_FACING_BACK
            }

            try {

                previewView.post {
                    bindCameraUseCases()
                }

            } catch (e: Exception) {
                // Do nothing
                e.printStackTrace()
            }
        }

        imageButtonTake.setOnClickListener {

            if(selectedImageList.size >= limit) {

                return@setOnClickListener
            }

            context?.let {

                showProgress()

                val photoFilePath = createFilePath(getOutputDirectory(it))
                val photoFile = File(photoFilePath)
                val metadata = ImageCapture.Metadata().apply {
                    // Mirror image when using the front camera
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }

                ILog.debug(TAG, photoFile.absolutePath)
                val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(metadata).build()

                imageCapture.takePicture(
                    outputFileOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {

                        override fun onError(error: ImageCaptureException) {
                            ILog.debug(TAG, error.message)
                            error.printStackTrace()
                            ThreadUtility.startUIThread(0) {
                                hideProgress()
                            }
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                            val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                            ILog.debug(TAG, savedUri)

                            ThreadUtility.startThread {

                                AudioManagerUtility.setNoMute()
                                MediaPlayerUtility.play()

                                ThreadUtility.startUIThread(0) {

                                    addImage(savedUri)

                                    togglePreviewThumbnail(savedUri)

                                    hideProgress()
                                }
                            }
                        }
                    })
            }
        }

        textViewAction.setOnClickListener {

            context?.let { context ->

                if(textViewAction.text == getString(R.string.camera_cancel)) {
                    activity?.finish()
                    return@setOnClickListener
                }

                if (selectedImageList.isNotEmpty()) {

                    val list = mutableListOf<Uri>()
                    for (item in selectedImageList) {
                        list.add(0, item.imageUri)
                    }

                    val pathList = SHCameraPhotoResultProcessor.uriListToCacheFilePathList(context, list)

                    // ok
                    HashMap<String, Any>().apply {
                        this["list"] = pathList
                        this["fromWhere"] = fromWhere
                    }.also { hashMap ->
                        ILog.debug(TAG, "send image $pathList size")

                        EventCenter.sendEvent(ESSArrows.SET_SELECTED_IMAGE_LIST, this, hashMap)
                    }
                }

                activity?.finish()

            }

        }
    }

    private fun addImage(imageUri: Uri) {
        val imageSelectorItemBean = ImageSelectedItemBean()
        imageSelectorItemBean.imageUri = imageUri
        imageSelectorItemBean.isSelected = true
        selectedImageList.add(0, imageSelectorItemBean)

        shSelectedImageViewHolder?.insert(imageSelectorItemBean)
    }

    private fun updateFlashImage() {
        if (flash) {
            imageButtonFlash.setImageResource(R.mipmap.flash_auto)
        }
        else {
            imageButtonFlash.setImageResource(R.mipmap.flash_off)
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun initCamera(activity: FragmentActivity) {

        cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({

            initCamera()
            checkBackFrontCamera()
            bindCameraUseCases()

        }, ContextCompat.getMainExecutor(activity))
    }

    private fun checkBackFrontCamera() {

        if(!hasBackCamera() && !hasFrontCamera()) {
            activity?.finish()
        }

        if(hasBackCamera() && hasFrontCamera()) {
            imageButtonSwitchCamera.visibility = View.VISIBLE
        }
        else {
            imageButtonSwitchCamera.visibility = View.GONE
        }
    }

    private fun initCamera() {

        ILog.debug(TAG, "initCamera")

        cameraProvider = cameraProviderFuture.get()
        cameraExecutor = Executors.newSingleThreadExecutor()
        displayManager.registerDisplayListener(displayListener, null)
    }

    private fun bindCameraUseCases() {

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
        ILog.debug(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        ILog.debug(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = previewView.display.rotation

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        preview = initPreview(screenAspectRatio, rotation)

        imageCapture = initImageCapture(screenAspectRatio, rotation)
//        imageCapture = initImageCapture(screenAspectRatio, rotation, ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)

        imageAnalysis = initImageAnalysis(screenAspectRatio, rotation)

        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalysis
            )

            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(previewView.surfaceProvider)
        } catch (e: Exception) {
            ILog.debug(TAG, "Use case binding failed ${e.message}")
        }
    }

    private fun initPreview(screenAspectRatio: Int, rotation: Int): Preview {

        return Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
    }

    private fun initImageCapture(screenAspectRatio: Int, rotation: Int): ImageCapture {

        val flashMode = if(flash) {
            ImageCapture.FLASH_MODE_AUTO
        }
        else {
            ImageCapture.FLASH_MODE_OFF
        }

        return ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(flashMode)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
    }

    private fun initImageAnalysis(screenAspectRatio: Int, rotation: Int): ImageAnalysis {


        return ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, NormalImageRealTimeAnalyzer(object :
                    NormalImageRealTimeAnalyzer.NormalImageRealTimeAnalyzerDelegate {
                    override fun onBitmap(bitmap: Bitmap, degree: Int) {

//                context?.let {
//                    val photoFilePath = createFilePath(getOutputDirectory(it), PHOTO_EXTENSION)
//
//                    ILog.debug(TAG, photoFilePath)
//
//                    val bufferedOutputStream = BufferedOutputStream(FileOutputStream(File(photoFilePath)))
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bufferedOutputStream)
//
//                    BitmapUtil.compressImageWithFilePath(photoFilePath, 1, degree)
//
//                    ThreadUtil.startUIThread(0) {
//
//                        imageView.setImageBitmap(BitmapUtil.rotate(bitmap, degree))
//
//                    }
//                }

                    }
                }))
            }
    }

    private fun hasBackCamera(): Boolean {
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    private fun hasFrontCamera(): Boolean {
        return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    private fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalCacheDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir
        }
        else {
            appContext.filesDir
        }
    }

    private fun createFilePath(baseFolder: File): String {

        return "${baseFolder}${DateUtility.getCurrentDateTimeSSSStringWithNoSpace("_")}$PHOTO_EXTENSION"
    }

    private fun showSelectedImageArea() {

        if(shSelectedImageViewHolder != null) {
            return
        }

        shSelectedImageViewHolder = SHSelectedImageViewHolder(
            context,
            object : SHSelectedImageViewHolder.SHSelectedImageViewHolderDelegate {
                override fun onDelete(imageSelectedItemBean: ImageSelectedItemBean) {

                    selectedImageList.remove(imageSelectedItemBean)

                    if (selectedImageList.isEmpty()) {
                        textViewImageCount.text = "0"
                        closeSelectedImageArea()
                        imageView.setImageBitmap(null)
                    } else {
                        textViewImageCount.text = selectedImageList.size.toString()

                        imageView.setImageURI(selectedImageList[0].imageUri)
                    }

                    textViewAction.text = if (selectedImageList.isEmpty()) {
                        getString(R.string.camera_cancel)
                    } else {
                        getString(R.string.camera_confirm)
                    }
                }

                override fun onClose() {
                    closeSelectedImageArea()
                }

            },
            selectedImageList
        ).apply {
            frameLayoutSelectedImageArea.addView(this.view)
            frameLayoutSelectedImageArea.visibility = View.VISIBLE
        }

    }

    private fun closeSelectedImageArea() {
        if(shSelectedImageViewHolder == null) {
            return
        }

        frameLayoutSelectedImageArea.removeAllViews()
        shSelectedImageViewHolder = null
        frameLayoutSelectedImageArea.visibility = View.GONE
    }

    private fun showAlbumSelector() {

        if (albumSelectorViewHolder != null) {
            return
        }

        context?.let {

            albumSelectorViewHolder = AlbumSelectorViewHolder(it, limit - selectedImageList.size, object :
                AlbumSelectorViewHolder.AlbumSelectorViewHolderDelegate {

                override fun onConfirm() {

                    albumSelectorViewHolder?.let {
                        val list = albumSelectorViewHolder!!.getSelectedImagePath()

                        for (imagePath in list) {
                            addImage(imagePath)
                        }

                        togglePreviewThumbnail(list[list.size - 1])

                    }

                    closeAlbumSelector()
                }

                override fun onClose() {
                    closeAlbumSelector()
                }
            }).apply {

                frameLayoutRoot.addView(this.view)

                this.view.visibility = View.VISIBLE
            }

        }

    }

    private fun togglePreviewThumbnail(imageUri: Uri) {

        imageView.setImageURI(imageUri)
        ILog.debug(TAG, selectedImageList.size.toString())
        textViewImageCount.text = selectedImageList.size.toString()

        textViewAction.text = if (selectedImageList.isEmpty()) {
            getString(R.string.camera_cancel)
        }
        else {
            getString(R.string.camera_confirm)
        }

    }

    private fun closeAlbumSelector() {
        if(albumSelectorViewHolder == null) {
            return
        }

        frameLayoutRoot.removeView(albumSelectorViewHolder!!.view)
        albumSelectorViewHolder = null
    }

    private fun showProgress() {
        frameLayoutProgress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        frameLayoutProgress.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            ThemeUtility.hideSystemUI(it)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.let {
            ThemeUtility.showSystemUI(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }
}