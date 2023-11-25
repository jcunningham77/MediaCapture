package mediacapture.io

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class MediaCaptureViewModel : ViewModel() {

    private val viewStateSubject = PublishSubject.create<ViewState>().startWith(PendingInitialization)

    val viewState: Observable<ViewState> = viewStateSubject.hide()

    sealed class ViewState
    object PendingInitialization : ViewState()

    class InitializationComplete(val processCameraProvider: ProcessCameraProvider) : ViewState()
}
