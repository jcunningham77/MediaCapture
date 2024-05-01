package mediacapture.io

import androidx.camera.lifecycle.ProcessCameraProvider
import io.reactivex.rxjava3.core.Single
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MediaCaptureViewModelTest {

    @Test
    fun `Happy path`() {

        val processCameraProviderMock = mock<ProcessCameraProvider>()
        val retrieveRecentMediaUseCase: RetrieveRecentMediaUseCase = mock()
        val processCameraProviderUseCase: ProcessCameraProviderUseCase = mock {
            on { invoke() } doReturn Single.just(processCameraProviderMock)
        }

        val viewModel =
            MediaCaptureViewModel(retrieveRecentMediaUseCase, processCameraProviderUseCase)

        val testObserver = viewModel.viewState.test()

        testObserver.assertEmpty()

        viewModel.permissionsGranted()

        testObserver.assertValue {
            it is MediaCaptureViewModel.Initialized
                    && it.recordingState == MediaCaptureViewModel.RecordingState.INITIALIZED
                    && it.cameraFacing == MediaCaptureViewModel.CameraFacing.FRONT
        }

        viewModel.onClick(MediaCaptureViewModel.FlipCameraClickEvent)

        testObserver.assertValueAt(1) { it is MediaCaptureViewModel.Initialized && it.cameraFacing == MediaCaptureViewModel.CameraFacing.BACK }
    }
}
