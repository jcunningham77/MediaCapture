package demoapp.io

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import demoapp.io.ui.theme.DemoAppTheme
import mediacapture.io.MediaCaptureActivity

class DemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoAppTheme {
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
        Row(
            modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 20.dp)
                )
        ) {
            IconButton(onClick = {
                val intent = Intent(this@DemoActivity, MediaCaptureActivity::class.java)
                startActivity(intent)
            }, modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth(.10f), content = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_emoji_menu),
                    contentDescription = "Camera Button",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            })
            BasicTextField(
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSecondary),
                value = "Enter Text here...",
                onValueChange = {},
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(.85f)
                    .padding(start = 5.dp, top = 5.dp, bottom = 5.dp, end = 5.dp)
            )
            IconButton(
                onClick = {
                    val intent = Intent(this@DemoActivity, MediaCaptureActivity::class.java)
                    startActivity(intent)
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                        contentDescription = "Camera Button",
                        tint = MaterialTheme.colorScheme.onSecondary

                    )
                },
            )
        }
    }

    @Preview
    @Composable
    fun InputRowPreview() {
        InputRow()
    }

}

