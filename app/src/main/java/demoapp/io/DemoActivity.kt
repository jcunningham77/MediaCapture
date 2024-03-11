package demoapp.io

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import demoapp.io.ui.theme.MediaCaptureTheme
import mediacapture.io.MediaCaptureActivity

class DemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaCaptureTheme {
                // A surface container using the 'background' color from the theme
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {

                    val (cameraButtonRef) = createRefs()
                    val inputBoxLayoutModifier = Modifier
                        .constrainAs(cameraButtonRef) {

                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                            start.linkTo(parent.start)
                        }
                        .padding(10.dp)

                    InputRow(inputBoxLayoutModifier)


                }
            }
        }
    }


    @Composable
    fun InputRow(modifier: Modifier = Modifier) {
        Row(modifier.background(Color.Blue, shape = RoundedCornerShape(5))) {
            TextField(
                value = "Enter Text here...",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(.80f)
            )
            IconButton(onClick = {
                val intent = Intent(this@DemoActivity, MediaCaptureActivity::class.java)
                startActivity(intent)
            }, modifier = Modifier, content = {
                Image(
                    painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                    contentDescription = "Camera Button"
                )
            })
        }
    }

    @Preview
    @Composable
    fun InputRowPreview() {
        InputRow()
    }

}

