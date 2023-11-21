package mediacapture.io

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import mediacapture.io.ui.theme.MediaCaptureTheme

class MediaCaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaCaptureTheme {
                // A surface container using the 'background' color from the theme
                BottomNavigation()
            }
        }
    }
}

@Composable
fun BottomNavigation() {

        Row(modifier = Modifier.height(100.dp)) {
            FlipCameraButton()
            RecordButton()
        }
}

@Composable
fun FlipCameraButton() {
    Image(
        painterResource(id = R.drawable.baseline_flip_camera_android_24),
        contentDescription = null
    )
}

@Composable
fun RecordButton() {
    Image(
        painterResource(id = R.drawable.baseline_fiber_manual_record_24),
        contentDescription = null
    )
}



