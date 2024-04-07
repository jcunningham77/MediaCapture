package mediacapture.io.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.toLiveData
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable


fun <T : Any> Observable<T>.observe(lifecycleOwner: LifecycleOwner, block: (T) -> Unit) {
    toFlowable(BackpressureStrategy.BUFFER).toLiveData()
        .observe(lifecycleOwner) { block.invoke(it!!) }

}

