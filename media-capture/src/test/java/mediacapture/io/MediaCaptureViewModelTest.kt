package mediacapture.io

import android.net.Uri
import androidx.camera.lifecycle.ProcessCameraProvider
import io.reactivex.rxjava3.core.Single
import mediacapture.io.model.Media
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MediaCaptureViewModelTest {

    @Test
    fun `Happy path`() {

        val processCameraProviderMock = mock<ProcessCameraProvider>()
        val retrieveRecentMediaUseCase: RetrieveRecentMediaUseCase = mock() {
            on { invoke() } doReturn createTestMedia()
        }
        val processCameraProviderUseCase: ProcessCameraProviderUseCase = mock {
            on { invoke() } doReturn Single.just(processCameraProviderMock)
        }

        val viewModel =
            MediaCaptureViewModel(retrieveRecentMediaUseCase, processCameraProviderUseCase)

        val viewStateTestObserver = viewModel.viewState.test()

        viewStateTestObserver.assertEmpty()

        viewModel.permissionsGranted()

        viewStateTestObserver.assertValue {
            it is MediaCaptureViewModel.Initialized
                    && it.recordingState == MediaCaptureViewModel.RecordingState.INITIALIZED
                    && it.cameraFacing == MediaCaptureViewModel.CameraFacing.FRONT
        }

        viewModel.onClick(MediaCaptureViewModel.FlipCameraClickEvent)

        viewStateTestObserver.assertValueAt(1) { it is MediaCaptureViewModel.Initialized && it.cameraFacing == MediaCaptureViewModel.CameraFacing.BACK && it.recordingState == MediaCaptureViewModel.RecordingState.INITIALIZED }


        viewModel.onClick(MediaCaptureViewModel.FlipCameraClickEvent)

        viewStateTestObserver.assertValueAt(2) { it is MediaCaptureViewModel.Initialized && it.cameraFacing == MediaCaptureViewModel.CameraFacing.FRONT && it.recordingState == MediaCaptureViewModel.RecordingState.INITIALIZED }


        viewModel.onClick(MediaCaptureViewModel.RecordClickEvent)

        viewStateTestObserver.assertValueAt(3) { it is MediaCaptureViewModel.Initialized && it.cameraFacing == MediaCaptureViewModel.CameraFacing.FRONT && it.recordingState == MediaCaptureViewModel.RecordingState.RECORDING }

        viewModel.onClick(MediaCaptureViewModel.StopClickEvent)

        viewStateTestObserver.assertValueAt(4) { it is MediaCaptureViewModel.Initialized && it.cameraFacing == MediaCaptureViewModel.CameraFacing.FRONT && it.recordingState == MediaCaptureViewModel.RecordingState.STOPPED }

        val mostRecentMediaTestObserver = viewModel.mostRecentMedia.test()

        viewModel.fetchMostRecentMedia()

        val expectedMedia = createTestMedia().first()

        mostRecentMediaTestObserver.assertValue { it.name == expectedMedia.name }

    }


    private fun createTestMedia(): List<Media> {
        val media = mutableListOf<Media>()
        for (i in 1..5) {
            media.add(
                Media(
                    uri = mock(),
                    name = "TestName$i",
                    duration = 50,
                    size = 100,
                    mediaStoreId = 200,
                    dateTakenMillis = 1718998879786
                )
            )
        }
        return media
    }
}
