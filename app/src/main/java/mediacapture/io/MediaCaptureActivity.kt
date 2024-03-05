package mediacapture.io

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.util.Consumer
import kotlinx.coroutines.delay
import mediacapture.io.livedata.observe
import mediacapture.io.ui.composables.ElapsedTimeView
import mediacapture.io.ui.composables.FlipCameraButton

@SuppressLint("ModifierParameter")
class MediaCaptureActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: MediaCaptureViewModel
    private lateinit var camera: Camera

    companion object {
        const val VIDEO_URI = "video.uri"
        const val VIDEO_MAX_LENGTH = 60
    }

    private val mutableViewState: MutableState<MediaCaptureViewModel.ViewState> =
        mutableStateOf(MediaCaptureViewModel.PendingInitialization)

    private val mutableMediaListState: MutableState<List<Media>> =
        mutableStateOf(emptyList())

    // region camera x members
    private var recording: Recording? = null
    private var pendingRecording: PendingRecording? = null

    private fun createRecordingListener(): Consumer<VideoRecordEvent> {
        return Consumer<VideoRecordEvent> { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    Log.i(TAG, "createRecordingListener: JEFFREYCUNNINGHAM Video capture begins:")
                }

                is VideoRecordEvent.Finalize -> {
                    Log.i(TAG, "createRecordingListener: JEFFREYCUNNINGHAM Video Finalize:")
                    if (!event.hasError()) {
                        // update app internal state
                        Log.i(
                            TAG,
                            "createRecordingListener: JEFFREYCUNNINGHAM Video capture succeeded: ${event.outputResults.outputUri}"
                        )
                        val result = bundleOf(
                            VIDEO_URI to event.outputResults.outputUri
                        )
                        Log.i(
                            TAG,
                            "createRecordingListener: JEFFREYCUNNINGHAM Video capture ends with success:  ${event.outputResults}"
                        )

                    } else {
                        Log.i(
                            TAG,
                            "createRecordingListener Video capture ends with error: 1 JEFFREYCUNNINGHAM ${event.error}"
                        )

                        Log.e(
                            TAG,
                            event.cause?.message,
                            event.cause
                        )
                        recording = null
                    }
                }

                is VideoRecordEvent.Status -> {
                }

                is VideoRecordEvent.Pause -> {
                    Log.i(TAG, "createRecordingListener: JEFFREYCUNNINGHAM Video capture paused:")
                }

                is VideoRecordEvent.Resume -> {
                    Log.i(TAG, "createRecordingListener: JEFFREYCUNNINGHAM Video capture resume:")
                }
            }

        }
    }

    private fun bindPreview(
        viewState: MediaCaptureViewModel.Initialized,
        previewView: PreviewView,
        context: Context,
        activity: ComponentActivity,

        ): PendingRecording {
        Log.i(TAG, "JEFFREYCUNNINGHAM: bindPreview: viewState: $viewState")
        val preview: Preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)


        val selector = QualitySelector.from(
            Quality.UHD, FallbackStrategy.higherQualityOrLowerThan(
                Quality.SD
            )
        )
        val recorder = Recorder.Builder().setQualitySelector(selector).build()
        val videoCapture = VideoCapture.withOutput(recorder)
        val cameraSelector = when (viewState.cameraFacing) {
            MediaCaptureViewModel.CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            MediaCaptureViewModel.CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
        }
        viewState.processCameraProvider.unbindAll()

        camera = viewState.processCameraProvider.bindToLifecycle(
            activity,
            cameraSelector,
            videoCapture,
            preview
        )

        // todo handle the permissions more gracefully
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                (context as Activity?)!!,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )

        }

        Log.i(TAG, "JEFFREYCUNNINGHAM: bindPreview: camera = $camera")
        return videoCapture.output.prepareRecording(context, createMediaStoreOptions(activity))
            .withAudioEnabled()
    }

    private fun createMediaStoreOptions(activity: Activity): MediaStoreOutputOptions {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "CameraX-VideoCapture-2")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        }
        return MediaStoreOutputOptions.Builder(
            activity.application.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()
    }

    private fun startRecording() {
        recording = pendingRecording!!.start(
            ContextCompat.getMainExecutor(this.baseContext),
            createRecordingListener()
        )
    }

    private fun stopRecording() {
        if (recording != null) {
            recording!!.stop()
        } else {
            Log.i(TAG, "stopRecording:  recording is NULL")
        }
    }
    // endregion camera x members

    // region activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MediaCaptureViewModel(this.application)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            Log.d("-- CAMERA PERMISSION --", permission.toString())
        }.launch(Manifest.permission.CAMERA)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            Log.d("-- RECORD_AUDIO PERMISSION --", permission.toString())
        }.launch(Manifest.permission.RECORD_AUDIO)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()

        viewModel.viewState.observe(this) {

            if (it is MediaCaptureViewModel.Initialized) {
                if (it.recordingState == MediaCaptureViewModel.RecordingState.RECORDING) {
                    startRecording()
                }
                if (it.recordingState == MediaCaptureViewModel.RecordingState.STOPPED) {
                    stopRecording()
                }
            }

            mutableViewState.value = it
        }

        mutableMediaListState.value = retrieveRecentMedia()

        setContent {
            ConstraintLayoutContent(mutableViewState, mutableMediaListState, this)
        }
    }
    // endregion activity lifecycle

    // region composable
    @Composable
    fun ConstraintLayoutContent(
        mutableViewState: MutableState<MediaCaptureViewModel.ViewState>,
        mutableMediaList: MutableState<List<Media>>,
        activity: ComponentActivity,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val viewState = mutableViewState.value
            val (previewSurfaceRef, thumbGalleryRef, flipCameraButtonRef, recordButtonRef, elapsedTimeRef) = createRefs()


            val previewModifier = Modifier.constrainAs(previewSurfaceRef) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
            when (viewState) {
                is MediaCaptureViewModel.PendingInitialization -> {
                    CircularProgressIndicator(previewModifier.size(200.dp))
                }

                is MediaCaptureViewModel.Initialized -> {
                    CameraPreview(
                        viewState,
                        activity
                    )
                }
            }

            ElapsedTimeView(layoutModifier = Modifier
                .constrainAs(elapsedTimeRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                }
                .padding(10.dp), mutableViewState)

            FlipCameraButton(
                Modifier
                    .constrainAs(flipCameraButtonRef) {
                        top.linkTo(recordButtonRef.top)
                        bottom.linkTo(recordButtonRef.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(recordButtonRef.start)
                    }
                    .size(40.dp, 40.dp)
            ) { viewModel.onClick(MediaCaptureViewModel.FlipCameraClickEvent) }

            // record button
            val recordButtonLayoutModifier = Modifier
                .constrainAs(recordButtonRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(10.dp)
                .size(100.dp, 100.dp)

            val thumbGalleryLayoutModifier = Modifier
                .constrainAs(thumbGalleryRef) {
                    bottom.linkTo(recordButtonRef.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(5.dp)
                .height(100.dp)

            if (mutableMediaList.value.isNotEmpty()) {
                LazyRow(modifier = thumbGalleryLayoutModifier) {
                    items(mutableMediaList.value) {
                        it.thumbnailUri?.let { thumbnail ->
                            if (it.mediaType == MediaType.VIDEO) {
                                Box() {
                                    Image(bitmap = thumbnail.asImageBitmap(), "Thumbnail")
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_videocam_24),
                                        contentDescription = "VideoType",
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(end = 5.dp),
                                    )
                                }
                            }

                        } ?: run {
                            Text(text = "Thumbnail unavailable")
                        }
                    }
                }
            }


            if (viewState is MediaCaptureViewModel.PendingInitialization
                || (viewState is MediaCaptureViewModel.Initialized &&
                        viewState.recordingState != MediaCaptureViewModel.RecordingState.STOPPED)
            ) {
                RecordButton(recordButtonLayoutModifier, mutableViewState)
            } else {
                Text(
                    "Recording complete!",
                    modifier = recordButtonLayoutModifier,
                    color = colorResource(R.color.white),
                    fontSize = TextUnit(20f, TextUnitType.Sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }


    @Composable
    fun CameraPreview(
        viewState: MediaCaptureViewModel.Initialized,
        activity: ComponentActivity,
    ) {

        Box(Modifier.fillMaxSize()) {
            AndroidView(modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                },
                update = {
                    if (viewState.recordingState == MediaCaptureViewModel.RecordingState.INITIALIZED) {
                        pendingRecording = bindPreview(
                            viewState,
                            it,
                            it.context,
                            activity,
                        )
                    }
                }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(.30F)
                    .background(colorResource(id = R.color.black_40))
            )
        }
    }

    @Composable
    fun RecordButton(
        layoutModifier: Modifier,
        mutableViewState: MutableState<MediaCaptureViewModel.ViewState>
    ) {

        val viewState = mutableViewState.value

        var clickListener: () -> Unit = {}
        var drawableInt = 0
        var enabled = false

        if (viewState is MediaCaptureViewModel.PendingInitialization) {
            drawableInt = R.drawable.baseline_fiber_manual_record_24
            enabled = false
        } else if (viewState is MediaCaptureViewModel.Initialized && viewState.recordingState == MediaCaptureViewModel.RecordingState.INITIALIZED) {
            clickListener = { viewModel.onClick(MediaCaptureViewModel.RecordClickEvent) }
            drawableInt = R.drawable.baseline_fiber_manual_record_24
            enabled = true
        } else if (viewState is MediaCaptureViewModel.Initialized && viewState.recordingState == MediaCaptureViewModel.RecordingState.RECORDING) { // we are recording
            clickListener = { viewModel.onClick(MediaCaptureViewModel.StopClickEvent) }
            drawableInt = R.drawable.baseline_stop_24
            enabled = true
        }

        Box(layoutModifier) {
            IconButton(
                onClick = clickListener,
                modifier = layoutModifier,
                enabled = enabled,
                content = {
                    Image(
                        painter = painterResource(id = drawableInt),
                        contentDescription = null,
                        modifier = layoutModifier
                            .size(100.dp)

                    )
                }
            )
            if (viewState is MediaCaptureViewModel.Initialized && viewState.recordingState == MediaCaptureViewModel.RecordingState.RECORDING) {
                var progress by remember {
                    mutableStateOf(0f)
                }

                LaunchedEffect(key1 = true, block = {
                    var elapsedSeconds = 0
                    while (true) {
                        elapsedSeconds++
                        progress = elapsedSeconds / VIDEO_MAX_LENGTH.toFloat()
                        delay(1000)
                    }
                })
                CircularProgressIndicator(
                    color = Color.Red, modifier = layoutModifier,
                    progress = progress
                )
            }
        }


    }


    // endregion composable
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun retrieveRecentMedia(): List<Media> {

        val contentResolver = application.contentResolver
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val mediaList = mutableListOf<Media>()

        val orderBy = MediaStore.Video.Media.DATE_TAKEN
        val sortByParam = "$orderBy DESC"
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortByParam,
        )?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)


            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )


                val thumbnail: Bitmap =
                    applicationContext.contentResolver.loadThumbnail(
                        contentUri, Size(640, 480), null
                    )

                mediaList += Media(contentUri, thumbnail, name, duration, size)
            }

            mediaList.forEach {
                Log.i(TAG, "onViewCreated: JEFFREYCUNNINGHAM video collected = $it")
            }
        }
        return mediaList
    }

    data class Media(
        val uri: Uri,
        val thumbnailUri: Bitmap? = null,
        val name: String,
        val duration: Int,
        val size: Int,
        val mediaType: MediaType = MediaType.VIDEO
    )

    enum class MediaType { VIDEO, PHOTO }

}
