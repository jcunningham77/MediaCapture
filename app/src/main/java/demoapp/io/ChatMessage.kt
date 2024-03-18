package demoapp.io

import android.net.Uri

sealed class ChatMessage

class TextMessage(val message: String) : ChatMessage()
class VideoMessage(uri: Uri) : ChatMessage()