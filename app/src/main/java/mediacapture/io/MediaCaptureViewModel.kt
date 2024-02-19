package mediacapture.io

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.camera.core.impl.utils.futures.FutureCallback
import androidx.camera.core.impl.utils.futures.Futures
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.AndroidViewModel
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
                        processCameraProvider,
                        recordingState = RecordingState.RECORDING
                    )
                )
                isRecordingState = true
            }

            StopClickEvent -> {
                viewStateSubject.onNext(
                    Initialized(
                        processCameraProvider,
                        recordingState = RecordingState.STOPPED
                    )
                )
                isRecordingState = false
            }
        }
    }

    sealed class ClickEvent

    object FlipCameraClickEvent : ClickEvent()

    object RecordClickEvent : ClickEvent()

    object StopClickEvent : ClickEvent()


    // endregion user events

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
    private val viewStateSubject =
        PublishSubject.create<ViewState>()

    val viewState: Observable<ViewState> = viewStateSubject.hide()

    // TODO default this to last used
    private var cameraFacingSelected = CameraFacing.FRONT

    // TODO we shouldn't need this member - make reactive
    private var isRecordingState = false

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

        val initializationViewStateObservable =
            Observable.interval(500, 500, TimeUnit.MILLISECONDS).take(2).subscribe {
                // FIXME
                // we should really be using startsWith() on viewStateSubject to emit a starting value, but that doesn't seem to work
                // with reactivestreams:2.6.2 (or the ComponentActivity lifecycle)
                if (it == 0L) {
                    viewStateSubject.onNext(PendingInitialization)
                } else if (it == 1L) {
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
            }

        disposables.add(initializationViewStateObservable)

    }
}
