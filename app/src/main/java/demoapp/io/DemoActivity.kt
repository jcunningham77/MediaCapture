package demoapp.io

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import demoapp.io.ui.theme.DemoAppTheme
import mediacapture.io.MediaCaptureActivity
import mediacapture.io.MediaCaptureActivity.Companion.MEDIA_EXTRA
import mediacapture.io.model.Media
import kotlin.random.Random

class DemoActivity : ComponentActivity() {
    private val TAG = this.javaClass.simpleName

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.i(TAG, "JEFFREYCUNNINGHAM: result: ")
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val media = intent?.getParcelableExtra(MEDIA_EXTRA, Media::class.java)
                Log.i(TAG, "JEFFREYCUNNINGHAM: :received media =  $media ")
                media?.let {
                    val videoChatMessage = VideoMessage(media.uri)
                    mutableChatMessages.value += videoChatMessage
                }

            }
        }

    private val mutableChatMessages: MutableState<List<ChatMessage>> = mutableStateOf(emptyList())

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScreenContent(mutableChatMessages)
            mutableChatMessages.value = generateSampleMessages()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun ScreenContent(
        chatMessages: MutableState<List<ChatMessage>> = mutableStateOf(
            generateSampleMessages()
        )
    ) {
        DemoAppTheme {
            // A surface container using the 'background' color from the theme

            Column {
                ChatContainer(Modifier.weight(1f), chatMessages)
                InputRow()
            }
        }
    }


    @Composable
    fun ChatContainer(
        modifier: Modifier = Modifier,
        chatMessages: MutableState<List<ChatMessage>>
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn {
                itemsIndexed(chatMessages.value) { index, item ->
                    val farUser = (index % 2) == 0
                    if (item is TextMessage) {
                        ChatItemBubble(item.message, farUser)
                    } else if (item is VideoMessage) {
                        VideoItemBubble(item.uri)
                    }

                }
            }
        }

        Log.i(TAG, "JEFFREYCUNNINGHAM: ChatContainer: messages =  ${generateSampleMessages()}")
    }

    private val farChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    private val nearChatBubbleShape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)

    @Composable
    fun ChatItemBubble(
        message: String,
        isUserMe: Boolean,
    ) {

        val backgroundBubbleColor = if (isUserMe) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.primary
        }

        val backgroundBubbleShape = if (isUserMe) {
            nearChatBubbleShape
        } else {
            farChatBubbleShape
        }

        // TODO: Do we really need a column here?
        Column(modifier = Modifier.padding(5.dp)) {
            Surface(
                color = backgroundBubbleColor,
                shape = backgroundBubbleShape
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(10.dp)

                )
            }

            // ...
        }
    }

    @Composable
    fun VideoItemBubble(
        uri: Uri
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).padding(5.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(.4f)
                    .align(Alignment.CenterEnd),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = nearChatBubbleShape
            ) {
                val context = LocalContext.current

                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .build()
                val exoPlayer = remember(context, mediaItem) {
                    SimpleExoPlayer.Builder(context)
                        .build()
                        .also { exoPlayer ->
                            exoPlayer.setMediaItem(mediaItem)
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = false
                            exoPlayer.repeatMode = REPEAT_MODE_OFF
                        }
                }

                DisposableEffect(
                    AndroidView(factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams
                                    .MATCH_PARENT,
                                ViewGroup.LayoutParams
                                    .MATCH_PARENT
                            )

                        }
                    }, modifier = Modifier.wrapContentSize())
                ) {
                    onDispose { exoPlayer.release() }
                }

            }
        }


    }

    @Composable
    private fun generateSampleMessages(): List<TextMessage> {

        val lipsum = stringResource(id = R.string.lipsum)
        val lipsumChunks = lipsum.split(" ")

        val messages = mutableListOf<TextMessage>()
        for (i in 1..4) {
            var message = String()
            while (message.length < 50) {
                val chunkToAppend = Random.nextInt(until = lipsumChunks.size - 1)
                message += " ${lipsumChunks[chunkToAppend]}"
            }
            messages.add(TextMessage(message))
        }
        return messages
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
    @Preview(name = "Light Mode")
    @Preview(
        uiMode = Configuration.UI_MODE_NIGHT_YES,
        showBackground = true,
        name = "Dark Mode"
    )
    @Composable
    fun FullScreenPreview() {
        ScreenContent()
    }
}

