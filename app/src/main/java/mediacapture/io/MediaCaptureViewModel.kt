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
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MediaCaptureViewModel(application: Application) : AndroidViewModel(application) {

    private val viewStateSubject =
        PublishSubject.create<ViewState>()


    private val disposables = CompositeDisposable()


    val viewState: Observable<ViewState> = viewStateSubject.hide()

    // TODO default this to last used
    private var cameraFacingSelected = CameraFacing.FRONT

    // TODO get context via UseCase and use regular ViewModel

    private val TAG = this.javaClass.simpleName

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

    // region user event
    fun onClick(clickEvent: ClickEvent) {
        Log.d(TAG, "onClick() JEFFREYCUNNINGHAM called with: clickEvent = $clickEvent")
        when (clickEvent) {
            FlipCameraClickEvent -> {
                val cameraFacing = cameraFacingSelected
                cameraFacingSelected = cameraFacing.getOther()
                viewStateSubject.onNext(CameraFlip(cameraFacing,processCameraProvider))
            }

            RecordClickEvent -> {
                viewStateSubject.onNext(IsRecording(processCameraProvider))
            }

            PauseClickEvent -> {
                viewStateSubject.onNext(IsPaused(processCameraProvider))
            }
        }
    }
    // endregion user events


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
                        viewStateSubject.onNext(Initialized(processCameraProvider))
                    })
                }
            }

        disposables.add(initializationViewStateObservable)

    }


    // region view state
    sealed class ViewState
    object PendingInitialization : ViewState()

    open class Initialized(open val processCameraProvider: ProcessCameraProvider) : ViewState()

    class IsRecording(override val processCameraProvider: ProcessCameraProvider) :
        Initialized(processCameraProvider)

    class IsPaused(override val processCameraProvider: ProcessCameraProvider) :
        Initialized(processCameraProvider)

    class CameraFlip(val cameraFacing: CameraFacing,override val processCameraProvider: ProcessCameraProvider) :
        Initialized(processCameraProvider)
    // endregion view state

    // region click events
    sealed class ClickEvent

    object FlipCameraClickEvent : ClickEvent()

    object RecordClickEvent : ClickEvent()

    object PauseClickEvent : ClickEvent()
    // endregion click events

    enum class CameraFacing {
        FRONT, BACK;


    }

    fun CameraFacing.getOther(): CameraFacing {
        return if (this == CameraFacing.FRONT) {
            CameraFacing.BACK
        } else {
            CameraFacing.FRONT
        }
    }
}
