package mediacapture.io

import androidx.camera.lifecycle.ProcessCameraProvider
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MediaCaptureViewModelTest {

    private val processCameraProviderUseCaseSubject = PublishSubject.create<ProcessCameraProvider>()

    @Test
    fun `Recording State emitted`() {

        val processCameraProviderMock = mock<ProcessCameraProvider>()
        val retrieveRecentMediaUseCase: RetrieveRecentMediaUseCase = mock()
        val processCameraProviderUseCase: ProcessCameraProviderUseCase = mock {
            on { invoke() } doReturn Single.just(processCameraProviderMock)
        }

        val viewModel = MediaCaptureViewModel(retrieveRecentMediaUseCase, processCameraProviderUseCase)

        val testObserver = viewModel.viewState.test()

        testObserver.assertEmpty()

        viewModel.permissionsGranted()

        testObserver.assertValue {
            it is MediaCaptureViewModel.Initialized
                    && it.recordingState == MediaCaptureViewModel.RecordingState.INITIALIZED
                    && it.cameraFacing == MediaCaptureViewModel.CameraFacing.FRONT
        }
    }
}
