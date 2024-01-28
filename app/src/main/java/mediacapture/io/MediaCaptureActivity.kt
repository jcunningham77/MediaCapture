package mediacapture.io

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.util.Consumer
import mediacapture.io.livedata.observe

class MediaCaptureActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: MediaCaptureViewModel

    companion object {
        const val VIDEO_URI = "video.uri"
    }

    // region camera x members
//    private lateinit var processCameraProvider: ProcessCameraProvider
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
                            "createRecordingListener Video capture ends with error: JEFFREYCUNNINGHAM ${event.error}"
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
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView,
        context: Context,
        activity: ComponentActivity,
    ): PendingRecording {
        val preview: Preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)


        val selector = QualitySelector.from(
            Quality.UHD, FallbackStrategy.higherQualityOrLowerThan(
                Quality.SD
            )
        )
        val recorder = Recorder.Builder().setQualitySelector(selector).build()
        val videoCapture = VideoCapture.withOutput(recorder)
        val camera = cameraProvider.bindToLifecycle(
            activity,
            CameraSelector.DEFAULT_FRONT_CAMERA,
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
    // endregion camera x members

    // region activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MediaCaptureViewModel(this.application)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
            Log.d("-- CAMERA PERMISSION --", permission.toString())
        }.launch(Manifest.permission.CAMERA)
    }

    override fun onResume() {
        super.onResume()

        viewModel.viewState.observe(this) {
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
            Log.i(TAG, "JEFFREYCUNNINGHAM: ConstraintLayoutContent: viewstate: $viewState")
            when (viewState) {
                is MediaCaptureViewModel.PendingInitialization -> {
                    LoadingIndicator(modifier = previewModifier, context = null)

                }

                is MediaCaptureViewModel.Initialized -> {

                    CameraPreview(
                        viewState.processCameraProvider,
                        previewModifier,
                        activity
                    )
                }
            }

            val controlGuideline = createGuidelineFromBottom(.20f)


            FlipCameraButton(
                Modifier
                    .constrainAs(flipCameraButtonRef) {
                        top.linkTo(recordButtonRef.top)
                        bottom.linkTo(recordButtonRef.bottom)
                        start.linkTo(parent.start)
                    }
                    .size(50.dp, 50.dp))

            // record or pause button
            val modifier = Modifier
                .constrainAs(recordButtonRef) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                }
                .size(100.dp, 100.dp)
            when (viewState) {
                is MediaCaptureViewModel.PendingInitialization -> {
                    RecordButton(modifier, false)
                }

                is MediaCaptureViewModel.Initialized -> {
                    if (!viewState.isRecording) {
                        RecordButton(modifier, true)
                    } else {
                        PauseButton(modifier)
                    }

                }
            }

        }
    }


    @Composable
    fun CameraPreview(
        cameraProvider: ProcessCameraProvider,
        modifier: Modifier,
        activity: ComponentActivity
    ) {
        Log.i(TAG, "JEFFREYCUNNINGHAM: CameraPreview: ")
        AndroidView(modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    post {
                        pendingRecording = bindPreview(
                            cameraProvider,
                            this,
                            context,
                            activity,
                        )
                    }
                }

            })

    }


    @Composable
    fun LoadingIndicator(modifier: Modifier, context: Context?) {
        if (context == null) {
            CircularProgressIndicator(modifier.size(200.dp))
        }
    }


    @Composable
    fun FlipCameraButton(modifier: Modifier) {
        IconButton(onClick = {
            viewModel.onClick(MediaCaptureViewModel.FlipCameraClickEvent)
        }, modifier = modifier,
            content = {
                Image(
                    painterResource(id = R.drawable.baseline_flip_camera_android_24),
                    contentDescription = null,
                    modifier = modifier.size(100.dp)
                )
            }
        )
    }

    @Composable
    fun RecordButton(modifier: Modifier, enabled: Boolean = true) {
        IconButton(

            onClick = {
                viewModel.onClick(MediaCaptureViewModel.RecordClickEvent)
            },
            modifier = modifier,
            enabled = enabled,
            content = {
                Image(
                    painter = painterResource(id = R.drawable.baseline_fiber_manual_record_24),
                    contentDescription = null,
                    modifier = modifier.size(100.dp)
                )

            }

        )
    }

    @Composable
    fun PauseButton(modifier: Modifier) {
        IconButton(
            onClick = {
                viewModel.onClick(MediaCaptureViewModel.PauseClickEvent)
            },
            modifier = modifier,
            content = {
                Image(
                    painter = painterResource(id = R.drawable.baseline_pause_circle_24),
                    contentDescription = null,
                    modifier = modifier.size(100.dp)
                )
            }
        )
    }
    // endregion composable

}



