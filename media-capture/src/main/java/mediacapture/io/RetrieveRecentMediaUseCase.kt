package mediacapture.io

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.ViewModelScoped
import mediacapture.io.model.Media
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class RetrieveRecentMediaUseCase @Inject constructor(private val  contentResolver: ContentResolver) :
    Function0<List<Media>> {
    private val TAG = this.javaClass.simpleName
    override operator fun invoke(): List<Media> {

//        val contentResolver = (context as Activity).contentResolver
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_TAKEN,
        )

        val mediaList = mutableListOf<Media>()

        val orderBy = MediaStore.Video.Media.DATE_TAKEN
        val sortByParam = "$orderBy DESC"
        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortByParam,
        )?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)


            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val dateTakenMillis = cursor.getLong(dateTakenColumn)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )

                mediaList += Media(
                    uri = contentUri,
                    name,
                    duration,
                    size,
                    mediaStoreId = id,
                    dateTakenMillis = dateTakenMillis
                )
            }

            mediaList.forEachIndexed { index, item ->
                val date = Date(item.dateTakenMillis)
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val formattedDate = format.format(date)
                Log.i(
                    TAG,
                    "retrieveRecentMedia: JEFFREYCUNNINGHAM video collected, index: $index,  uri: ${item.uri}, date Taken: $formattedDate"
                )
            }
        }
        return mediaList
    }
}