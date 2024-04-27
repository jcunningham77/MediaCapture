package mediacapture.io

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.impl.utils.futures.FutureCallback
import androidx.camera.core.impl.utils.futures.Futures
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.common.util.concurrent.ListenableFuture
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Named


class ProcessCameraProviderUseCase @Inject constructor(@Named("applicationContext") private val context: Context) :
    Function0<Single<ProcessCameraProvider>> {
    @SuppressLint("RestrictedApi")
    override fun invoke(): Single<ProcessCameraProvider> {
        val executorService = Executors.newSingleThreadExecutor()
        val listenableFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)

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