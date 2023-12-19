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


    // TODO get context via UseCase and use regular ViewModel

    private val TAG = this.javaClass.simpleName

    private val context = getApplication<Application>()
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    private val listenableFuture: ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(context)

    @SuppressLint("RestrictedApi")
    private val processCameraProviderSingle = Single.create<ProcessCameraProvider> {
        Log.i(TAG, "JEFFREYCUNNINGHAM: 1: ")
        Futures.addCallback(
            listenableFuture,

            object : FutureCallback<ProcessCameraProvider> {
                override fun onSuccess(result: ProcessCameraProvider?) {
                    Log.i(TAG, "JEFFREYCUNNINGHAM: 2: ")
                    it.onSuccess(result!!)
                }

                override fun onFailure(t: Throwable) {
                    Log.i(TAG, "JEFFREYCUNNINGHAM: onFailure: 3 error = $t")
                    it.onError(t)
                }

            }, executorService
        )
    }

    init {


        val initializationViewStateObservable =
            Observable.interval(2, 2, TimeUnit.SECONDS).take(2).subscribe {
                // FIXME
                // we should really be using startsWith() on viewStateSubject to emit a starting value, but that doesn't seem to work
                // with reactivestreams:2.6.2 (or the ComponentActivity lifecycle)
                if (it == 0L) {
                    Log.i(
                        TAG,
                        "JEFFREYCUNNINGHAM: pending2EmissionObservable: emission 1, sending PendingInitialization2"
                    )
                    viewStateSubject.onNext(PendingInitialization)
                } else if (it == 1L) {
                    Log.i(
                        TAG,
                        "JEFFREYCUNNINGHAM: pending2EmissionObservable: emission 2, subscribing to  processCameraProviderSingle"
                    )
                    disposables.add(processCameraProviderSingle.subscribe { processCameraProvider ->
                        Log.i(
                            TAG,
                            "JEFFREYCUNNINGHAM: :processCameraProvider = $processCameraProvider "
                        )
                        viewStateSubject.onNext(InitializationComplete(processCameraProvider))
                    })
                }
            }

        disposables.add(initializationViewStateObservable)

    }


    sealed class ViewState
    object PendingInitialization : ViewState()

    class InitializationComplete(val processCameraProvider: ProcessCameraProvider) : ViewState()
}
