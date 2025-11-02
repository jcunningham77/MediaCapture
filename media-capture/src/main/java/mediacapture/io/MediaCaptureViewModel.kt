package mediacapture.io

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import mediacapture.io.model.Media
import javax.inject.Inject

class MediaCaptureViewModel @Inject constructor(
    retrieveRecentMediaUseCase: RetrieveRecentMediaUseCase,
    processCameraProviderUseCase: ProcessCameraProviderUseCase,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private lateinit var processCameraProvider: ProcessCameraProvider

    // region user event
    fun onClick(clickEvent: ClickEvent) {
        println("JRC onClick called with: $clickEvent, stacktrace: ${Thread.currentThread().stackTrace[3]}")
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
                        processCameraProvider, recordingState = RecordingState.RECORDING, cameraFacing = cameraFacingSelected
                    )
                )
            }

            StopClickEvent -> {
                viewStateSubject.onNext(
                    Initialized(
                        processCameraProvider, recordingState = RecordingState.STOPPED, cameraFacing = cameraFacingSelected
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

    val existingMedia: Observable<List<Media>> = triggerMediaQuerySubject.flatMap {
        Observable.fromCallable {
            retrieveRecentMediaUseCase.invoke()
        }
    }

    private val fetchMostRecentMediaSubject = PublishSubject.create<Unit>()

    fun fetchMostRecentMedia() {
        fetchMostRecentMediaSubject.onNext(Unit)
    }

    val mostRecentMedia: Observable<Media> = fetchMostRecentMediaSubject.flatMap {
        Observable.fromCallable {
            retrieveRecentMediaUseCase.invoke()
        }
    }.map { list ->
        list.first()
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
        .doOnSubscribe { println("JRC viewState SUBSCRIBED") }
        .doOnDispose { println("JRC viewState DISPOSED") }
        .doOnEach { println("JRC viewState doOnEach = $it") }

    // TODO default this to last used
    // TODO probably should be replaced with subject
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
            disposables.add(processCameraProviderUseCase.invoke().subscribe { it ->
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
