package mediacapture.io

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.toLiveData
import io.reactivex.BackpressureStrategy


import mediacapture.io.ui.theme.MediaCaptureTheme

class MediaCaptureActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName
    private var viewModel = MediaCaptureViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaCaptureTheme {
                ConstraintLayoutContent()

            }
        }

        val publisher = viewModel.viewState.toFlowable(BackpressureStrategy.LATEST)

        publisher.toLiveData().observe(this) {
            Log.i(TAG, "JEFFREYCUNNINGHAM: onCreate: $it")
        }
    }
}

@Composable
fun ConstraintLayoutContent() {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        val (previewSurface, flipCameraButton, recordButton) = createRefs()

        val bottomGuideline = createGuidelineFromBottom(.20f)

        PreviewSurface(modifier = Modifier.constrainAs(previewSurface) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(bottomGuideline)
        }, context = null)

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
fun PreviewSurface(modifier: Modifier, context: Context?) {
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


