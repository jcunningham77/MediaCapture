package mediacapture.io

import org.junit.Test
import org.mockito.kotlin.mock

class MediaCaptureViewModelTest {

    @Test
    fun `Recording State emitted`() {
        val retrieveRecentMediaUseCase: RetrieveRecentMediaUseCase = mock()
        val processCameraProviderUseCase: ProcessCameraProviderUseCase = mock()

        val viewModel =
            MediaCaptureViewModel(retrieveRecentMediaUseCase, processCameraProviderUseCase)

        val testObserver = viewModel.viewState.test()

        testObserver.assertEmpty()

    }

}