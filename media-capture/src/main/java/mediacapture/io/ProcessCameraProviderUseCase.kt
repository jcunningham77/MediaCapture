package mediacapture.io

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.camera.core.impl.utils.futures.FutureCallback
import androidx.camera.core.impl.utils.futures.Futures
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.Executors
import javax.inject.Inject

@ViewModelScoped
class ProcessCameraProviderUseCase @Inject constructor(@ApplicationContext private val applicationContext: Context) :
    Function0<Single<ProcessCameraProvider>> {
    @SuppressLint("RestrictedApi")
    override fun invoke(): Single<ProcessCameraProvider> {
        val executorService = Executors.newSingleThreadExecutor()
        val listenableFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(applicationContext)

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