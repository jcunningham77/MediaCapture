package demoapp.io

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import demoapp.io.ui.theme.MediaCaptureTheme
import mediacapture.io.MediaCaptureActivity

public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaCaptureTheme {
                // A surface container using the 'background' color from the theme
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {

                    val (cameraButtonRef) = createRefs()
                    val cameraButtonModifier = Modifier
                        .constrainAs(cameraButtonRef) {

                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                        .padding(10.dp)

                    IconButton(onClick = {
                        val intent = Intent(this@MainActivity, MediaCaptureActivity::class.java)
                        startActivity(intent)
                    }, modifier = cameraButtonModifier, content = {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                            contentDescription = "Camera Button"
                        )
                    })

                }
            }
        }
    }


}

