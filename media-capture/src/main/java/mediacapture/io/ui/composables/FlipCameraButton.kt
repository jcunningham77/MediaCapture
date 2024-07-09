package mediacapture.io.ui.composables

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import mediacapture.io.R

@SuppressLint("ModifierParameter")
@Composable
fun FlipCameraButton(layoutModifier: Modifier, clickListener: () -> Unit) {

    var animationTrigger by remember {
        mutableStateOf(false)
    }
    val angle by animateFloatAsState(
        targetValue = if (animationTrigger) 360f else 0f,
        animationSpec = tween(500),
        label = "Rotation Angle"
    )

    IconButton(onClick = {
        clickListener.invoke()
        animationTrigger = !animationTrigger
    }, modifier = layoutModifier,
        content = {
            Image(
                painterResource(id = R.drawable.baseline_flip_camera_android_24),
                contentDescription = "Flip Camera Button",
                modifier = layoutModifier
                    .size(100.dp)
                    .rotate(angle)
            )
        }
    )
}