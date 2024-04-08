package mediacapture.io

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.impl.utils.futures.FutureCallback
import androidx.camera.core.impl.utils.futures.Futures
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.AndroidViewModel
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import mediacapture.io.model.Media
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MediaCaptureViewModel(application: Application) : AndroidViewModel(application) {


    private val disposables = CompositeDisposable()


    private val TAG = this.javaClass.simpleName

    // region camera x
    private lateinit var processCameraProvider: ProcessCameraProvider
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val listenableFuture: ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(application.applicationContext)

    @SuppressLint("RestrictedApi")
    private val processCameraProviderSingle = Single.create<ProcessCameraProvider> {

        Futures.addCallback(
            listenableFuture,

            object : FutureCallback<ProcessCameraProvider> {
                override fun onSuccess(result: ProcessCameraProvider?) {
                    Log.i(TAG, "JEFFREYCUNNINGHAM: onSuccess: $result")
                    it.onSuccess(result!!)
                }

                override fun onFailure(t: Throwable) {
                    Log.i(TAG, "JEFFREYCUNNINGHAM: onFailure: error = $t")
                    it.onError(t)
                }

            }, executorService
        )
    }
    // endregion camera x


    // region user event
    fun onClick(clickEvent: ClickEvent) {
        Log.d(TAG, "onClick() JEFFREYCUNNINGHAM called with: clickEvent = $clickEvent")
        when (clickEvent) {
            FlipCameraClickEvent -> {
                val cameraFacing = cameraFacingSelected.getOther()
                cameraFacingSelected = cameraFacing
                viewStateSubject.onNext(
                    Initialized(
                        processCameraProvider,
                        recordingState = RecordingState.INITIALIZED,
                        cameraFacing
                    )
                )
            }

            RecordClickEvent -> {
                viewStateSubject.onNext(
                    Initialized(
                        processCameraProvider, recordingState = RecordingState.RECORDING
                    )
                )
            }

            StopClickEvent -> {
                viewStateSubject.onNext(
                    Initialized(
                        processCameraProvider, recordingState = RecordingState.STOPPED
                    )
                )
            }
        }
    }

    private val permissionsGrantedSubject = PublishSubject.create<Unit>()
    fun permissionsGranted() {
        permissionsGrantedSubject.onNext(Unit)
    }

    sealed class ClickEvent

    object FlipCameraClickEvent : ClickEvent()

    object RecordClickEvent : ClickEvent()

    object StopClickEvent : ClickEvent()


    // endregion user events


    // region MediaStore

    private val triggerMediaQuerySubject = PublishSubject.create<Unit>()

    fun triggerMediaQuery() {
        triggerMediaQuerySubject.onNext(Unit)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    val existingMedia: Observable<List<Media>> = triggerMediaQuerySubject.flatMap {
        Observable.fromCallable {
            retrieveRecentMedia()
        }
    }

    private val fetchMostRecentMediaSubject = PublishSubject.create<Unit>()

    fun fetchMostRecentMedia() {
        fetchMostRecentMediaSubject.onNext(Unit)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    val mostRecentMedia: Observable<Media> = fetchMostRecentMediaSubject.flatMap {
        Observable.fromCallable {
            retrieveRecentMedia()
        }
    }.map { list ->
        list.first()
    }

    private val contentResolver = application.contentResolver

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun retrieveRecentMedia(): List<Media> {

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
    // endregion MediaStore

    enum class CameraFacing {
        FRONT, BACK;
    }

    private fun CameraFacing.getOther(): CameraFacing {
        return if (this == CameraFacing.FRONT) {
            CameraFacing.BACK
        } else {
            CameraFacing.FRONT
        }
    }

    // region view state
    private val viewStateSubject = PublishSubject.create<ViewState>()

    val viewState: Observable<ViewState> = viewStateSubject.hide()

    // TODO default this to last used
    private var cameraFacingSelected = CameraFacing.FRONT

    sealed class ViewState
    object PendingInitialization : ViewState()

    open class Initialized(
        open val processCameraProvider: ProcessCameraProvider,
        open val recordingState: RecordingState,
        open val cameraFacing: CameraFacing = CameraFacing.FRONT,
    ) : ViewState() {
        override fun toString(): String =
            "Initialized.ViewState, recordingState: $recordingState, cameraFacing: $cameraFacing"
    }

    enum class RecordingState {
        INITIALIZED, // no recording has yet been attempted
        RECORDING, // currently recording
        STOPPED // was recording but is now stopped
    }

    // endregion view state

    init {

        val initializationViewStateDisposable = permissionsGrantedSubject.subscribe {
            Log.i(
                TAG, "JEFFREYCUNNINGHAM: permissions have been granted, initializing Camera X:: "
            )
            disposables.add(processCameraProviderSingle.subscribe { it ->
                processCameraProvider = it
                viewStateSubject.onNext(
                    Initialized(
                        processCameraProvider,
                        recordingState = RecordingState.INITIALIZED,
                        cameraFacing = cameraFacingSelected,
                    )
                )
            })
        }


        disposables.add(initializationViewStateDisposable)

    }
}
