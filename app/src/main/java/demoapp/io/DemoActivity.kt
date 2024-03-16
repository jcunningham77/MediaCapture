package demoapp.io

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import mediacapture.io.model.Media

class DemoActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.i(TAG, "JEFFREYCUNNINGHAM: result: ")
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val media = intent?.getParcelableExtra("media_extra", Media::class.java)
                Log.i(TAG, "JEFFREYCUNNINGHAM: :received media =  $media ")
            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                // no op //

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
                    startForResult.launch(intent)
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Preview
    @Composable
    fun InputRowPreview() {
        InputRow()
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i(TAG, "JEFFREYCUNNINGHAM: onDestroy: ")
    }

}

