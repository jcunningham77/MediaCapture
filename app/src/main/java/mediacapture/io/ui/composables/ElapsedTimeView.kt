package mediacapture.io.ui.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import mediacapture.io.MediaCaptureViewModel
import mediacapture.io.R
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SuppressLint("ModifierParameter")
@Composable
fun ElapsedTimeView(
    layoutModifier: Modifier,
    mutableViewState: MutableState<MediaCaptureViewModel.ViewState>
) {
    var elapsedTime by remember {
        mutableStateOf(0L)
    }
    LaunchedEffect(key1 = mutableViewState.value, block = {
        val viewState = mutableViewState.value
        while (viewState is MediaCaptureViewModel.Initialized && viewState.recordingState == MediaCaptureViewModel.RecordingState.RECORDING) {
            elapsedTime++
            delay(1000)
        }
    })

    Box(
        contentAlignment = Alignment.Center,
        modifier = layoutModifier
            .wrapContentWidth()
            .defaultMinSize(60.dp)
            .height(20.dp)
            .background(
                color = Color(R.color.black_40),
                shape = RoundedCornerShape(60.dp)
            ),
    ) {
        Text(
            text = elapsedTime.formatForElapsedTimeView(),
            color = Color.White,
            fontSize = TextUnit(14f, TextUnitType.Sp)
        )
    }
}

private fun Long.formatForElapsedTimeView(): String {
    val duration = this.toDuration(DurationUnit.SECONDS)
    return duration.toComponents { minutes, seconds, _ ->
        "%02d:%02d".format(minutes, seconds)
    }
}