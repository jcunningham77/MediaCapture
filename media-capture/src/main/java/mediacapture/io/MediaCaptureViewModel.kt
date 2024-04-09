package mediacapture.io

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.impl.utils.futures.FutureCallback
import androidx.camera.core.impl.utils.futures.Futures
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.AndroidViewModel
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import mediacapture.io.model.Media
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.Q)
class MediaCaptureViewModel(
    application: Application,
    retrieveRecentMediaUseCase: RetrieveRecentMediaUseCase
) : AndroidViewModel(application) {

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
    @RequiresApi(Build.VERSION_CODES.Q)
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
            retrieveRecentMediaUseCase.invoke()
        }
    }

    private val fetchMostRecentMediaSubject = PublishSubject.create<Unit>()

    fun fetchMostRecentMedia() {
        fetchMostRecentMediaSubject.onNext(Unit)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    val mostRecentMedia: Observable<Media> = fetchMostRecentMediaSubject.flatMap {
        Observable.fromCallable {
            retrieveRecentMediaUseCase.invoke()
        }
    }.map { list ->
        list.first()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    // endregion MediaStore

    enum class CameraFacing {
        FRONT, BACK;
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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
    @RequiresApi(Build.VERSION_CODES.Q)
    private var cameraFacingSelected = CameraFacing.FRONT

    sealed class ViewState
    object PendingInitialization : ViewState()

    open class Initialized @RequiresApi(Build.VERSION_CODES.Q) constructor(
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
