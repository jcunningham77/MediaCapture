package mediacapture.io

import android.annotation.SuppressLint
import android.app.Application
import androidx.camera.core.impl.utils.futures.FutureCallback
import androidx.camera.core.impl.utils.futures.Futures
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.Executors


class ProcessCameraProviderUseCase(private val application: Application) :
    Function0<Single<ProcessCameraProvider>> {
    @SuppressLint("RestrictedApi")
    override fun invoke(): Single<ProcessCameraProvider> {
        val executorService = Executors.newSingleThreadExecutor()
        val listenableFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(application.applicationContext)

        return Single.create {
            Futures.addCallback(
                listenableFuture,

                object : FutureCallback<ProcessCameraProvider> {
                    override fun onSuccess(result: ProcessCameraProvider?) {
                        it.onSuccess(result!!)
                    }

                    override fun onFailure(t: Throwable) {
                        it.onError(t)
                    }

                }, executorService
            )
        }
    }

}