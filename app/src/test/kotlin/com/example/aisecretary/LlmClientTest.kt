import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LlamaClientTest {

    @Mock
    private lateinit var mockOllamaService: OllamaService

    private lateinit var llamaClient: LlamaClient

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        llamaClient = LlamaClient(mockOllamaService)
    }

    @Test
    fun testGetResponse() {
        val userInput = "Hello, how can you assist me?"
        val expectedResponse = "I can help you with various tasks."

        // Mock the behavior of the OllamaService
        whenever(mockOllamaService.sendRequest(userInput)).thenReturn(expectedResponse)

        val actualResponse = llamaClient.getResponse(userInput)

        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun testGetResponseWithEmptyInput() {
        val userInput = ""
        val expectedResponse = "Please provide a valid input."

        val actualResponse = llamaClient.getResponse(userInput)

        assertEquals(expectedResponse, actualResponse)
    }
}