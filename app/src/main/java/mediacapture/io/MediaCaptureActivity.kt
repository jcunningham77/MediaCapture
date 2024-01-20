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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import mediacapture.io.livedata.observe
import mediacapture.io.ui.theme.MediaCaptureTheme

class MediaCaptureActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName
    private lateinit var viewModel: MediaCaptureViewModel


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
}

@Composable
fun ConstraintLayoutContent(
    viewState: MediaCaptureViewModel.ViewState,
    activity: ComponentActivity,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        val (previewSurface, flipCameraButton, recordButton) = createRefs()

        val bottomGuideline = createGuidelineFromBottom(.20f)

        val previewModifier = Modifier.constrainAs(previewSurface) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(bottomGuideline)
        }

        if (viewState is MediaCaptureViewModel.PendingInitialization) {
            LoadingIndicator(modifier = previewModifier, context = null)
        } else if (viewState is MediaCaptureViewModel.InitializationComplete) {

            CameraPreview(
                viewState.processCameraProvider,
                previewModifier,
                activity
            )
        }



        FlipCameraButton(
            Modifier
                .constrainAs(flipCameraButton) {
                    top.linkTo(recordButton.top)
                    bottom.linkTo(recordButton.bottom)
                    start.linkTo(parent.start)

                }
                .size(50.dp, 50.dp))

        RecordButton(
            Modifier
                .constrainAs(recordButton) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)

                }
                .size(100.dp, 100.dp))
    }

}

@Composable
fun CameraPreview(
    cameraProvider: ProcessCameraProvider,
    modifier: Modifier,
    activity: ComponentActivity
) {
    val TAG = "CameraPreview"

    AndroidView(modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {


                implementationMode = PreviewView.ImplementationMode.COMPATIBLE

                post {

                    val pendingRecording = bindPreview(
                        cameraProvider,
                        this,
                        context,
                        activity
                    )

                    Log.i(
                        TAG,
                        "JEFFREYCUNNINGHAM: CameraPreview: STEP3: pendingRecoding = $pendingRecording"
                    )
                }
            }
        })

}

private fun bindPreview(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    context: Context,
    activity: ComponentActivity,
): PendingRecording? {

    val TAG = "bindPreview"

    val preview: Preview = Preview.Builder().build()
    preview.setSurfaceProvider(previewView.surfaceProvider)
    Log.i(
        TAG,
        "JEFFREYCUNNINGHAM: bindPreview: STEP1: preview: $preview has its surface provider set to previewView's surfaceProvider, i.e.: ${previewView.surfaceProvider}"
    )

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

    Log.i(
        TAG,
        "JEFFREYCUNNINGHAM: bindPreview: STEP2: camera has been bound to processCameraProvider's lifecycle. camera: $camera"
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


@Composable
fun LoadingIndicator(modifier: Modifier, context: Context?) {
    if (context == null) {
        CircularProgressIndicator(modifier.size(200.dp))
    }
}


@Composable
fun FlipCameraButton(modifier: Modifier) {
    Image(
        painterResource(id = R.drawable.baseline_flip_camera_android_24),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun RecordButton(modifier: Modifier) {
    Image(
        painterResource(id = R.drawable.baseline_fiber_manual_record_24),
        contentDescription = null,
        modifier = modifier,
    )
}


