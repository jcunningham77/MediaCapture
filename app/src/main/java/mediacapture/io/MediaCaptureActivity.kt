package mediacapture.io

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.lifecycle.LifecycleOwner
import mediacapture.io.livedata.observe

class MediaCaptureActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: MediaCaptureViewModel

    companion object {
        const val VIDEO_URI = "video.uri"
    }

    // region camera x members
    private var recording: Recording? = null
    private var pendingRecording: PendingRecording? = null

    private fun createRecordingListener(): Consumer<VideoRecordEvent> {
        return Consumer<VideoRecordEvent> { event ->
//            Log.i(TAG, "onViewCreated: JEFFREYCUNNINGHAM event = ${event.javaClass.simpleName} ****")
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
                        // update app state when the capture failed.
//                        preparedRecording?.close()

                        Log.i(
                            TAG,
                            "createRecordingListener Video capture ends with error: 1 JEFFREYCUNNINGHAM ${event.error}"
                        )

                        Log.e(
                            "createRecordingListener Video capture ends with error: 2 JEFFREYCUNNINGHAM",
                            event.cause?.message,
                            event.cause
                        )
//                        recording = null
                    }
                }

                is VideoRecordEvent.Status -> {
//                    Log.i(TAG, "onViewCreated: JEFFREYCUNNINGHAM Video capture status:")
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
        lifecycleOwner: LifecycleOwner,

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

        val camera = viewState.processCameraProvider.bindToLifecycle(
            lifecycleOwner,
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

            setContent {
                ConstraintLayoutContent(it, this)
            }
        }
    }
    // endregion activity lifecycle


    // region composable
    @Composable
    fun ConstraintLayoutContent(
        viewState: MediaCaptureViewModel.ViewState,
        activity: ComponentActivity,
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (previewSurfaceRef, flipCameraButtonRef, recordButtonRef) = createRefs()


            val previewModifier = Modifier.constrainAs(previewSurfaceRef) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }

            val controlGuideline = createGuidelineFromBottom(.20f)

            Log.i(TAG, "JEFFREYCUNNINGHAM: ConstraintLayoutContent: viewstate: $viewState")
            when (viewState) {
                is MediaCaptureViewModel.PendingInitialization -> {
                    LoadingIndicator(modifier = previewModifier, context = null)

                }

                is MediaCaptureViewModel.Initialized -> {

                    CameraPreview(
                        viewState,
                        activity
                    )
                }
            }

            FlipCameraButton(
                Modifier
                    .constrainAs(flipCameraButtonRef) {
                        top.linkTo(recordButtonRef.top)
                        bottom.linkTo(recordButtonRef.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(recordButtonRef.start)
                    }
                    .size(40.dp, 40.dp)
            )

            // record button
            val modifier = Modifier
                .constrainAs(recordButtonRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                }
                .size(100.dp, 100.dp)

            if (viewState is MediaCaptureViewModel.PendingInitialization
                || (viewState is MediaCaptureViewModel.Initialized && viewState.recordingState != MediaCaptureViewModel.RecordingState.STOPPED)
            ) {
                RecordButton(modifier, viewState)
            } else {
                Text(
                    "Recording complete!",
                    modifier = modifier,
                    color = colorResource(R.color.white),
                    fontSize = TextUnit(20f, TextUnitType.Sp),
                    textAlign = TextAlign.Center
                )
            }


        }
    }


    @Composable
    fun CameraPreview(
        viewState: MediaCaptureViewModel.ViewState,
        activity: ComponentActivity,
    ) {

        val lifecycleOwner = LocalLifecycleOwner.current
        Box(Modifier.fillMaxSize()) {
            AndroidView(modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PreviewView(context).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                },
                update = {
                    pendingRecording = bindPreview(
                        viewState as MediaCaptureViewModel.Initialized,
                        it,
                        it.context,
                        activity,
                        lifecycleOwner,
                    )
                }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(.20F)
                    .background(colorResource(id = R.color.black_40))
            )
        }
    }


    @Composable
    fun LoadingIndicator(modifier: Modifier, context: Context?) {
        if (context == null) {
            CircularProgressIndicator(modifier.size(200.dp))
        }
    }


    @Composable
    fun FlipCameraButton(modifier: Modifier) {


        var animationTrigger by remember {
            mutableStateOf(false)
        }
        val angle by animateFloatAsState(
            targetValue = if (animationTrigger) 360f else 0f,
            animationSpec = tween(500),
            label = "Rotation Angle"
        )

        IconButton(onClick = {
            viewModel.onClick(MediaCaptureViewModel.FlipCameraClickEvent)
            animationTrigger = !animationTrigger
        }, modifier = modifier,
            content = {
                Log.i(TAG, "JEFFREYCUNNINGHAM: FlipCameraButton: angle = $angle")
                Image(
                    painterResource(id = R.drawable.baseline_flip_camera_android_24),
                    contentDescription = null,
                    modifier = modifier
                        .size(100.dp)
                        .rotate(angle)
                )
            }
        )
    }

    @Composable
    fun RecordButton(
        modifier: Modifier,
        viewState: MediaCaptureViewModel.ViewState
    ) {

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

        IconButton(
            onClick = clickListener,
            modifier = modifier,
            enabled = enabled,
            content = {
                Image(
                    painter = painterResource(id = drawableInt),
                    contentDescription = null,
                    modifier = modifier.size(100.dp)
                )
            }
        )
    }
    // endregion composable
}
