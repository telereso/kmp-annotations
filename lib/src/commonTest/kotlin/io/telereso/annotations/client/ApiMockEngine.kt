import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.*
import io.telereso.kmp.core.Task
import io.telereso.kmp.core.await

data class ApiMockEngineParams(
    val content: Task<String>,
    val status: HttpStatusCode = HttpStatusCode.OK,
    val headers: Headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
    val encodedPath:String? = null
)


/**
 * we can make this class as builder so we can pass params and different configuraiton per test run e. HTTP response and Path values
 * or we can use a Data approach like below.
 */
class ApiMockEngine(val apiMockEngineParams: ApiMockEngineParams) {
    /**
     * The get() method is just a helper to easily access the engine property of an ApiMockEngine instance
     */
    fun get() = client.engine

    /**
     * responseHeaders defines a standard Content-Type: application/json header to be returned by the mock engine.
     */
    private val client = HttpClient(MockEngine) {
        engine {
            /**
             * Basically, when ApiMockEngine is used to make HTTP calls it will return the specific {Model}MockResponse object (as a JSON string)
             * when the URL path corresponds to the associated {resource}.
             * An HTTP OK (200) code and a standard Content-Type: application/json header will also be part of the response payload.
             */
            addHandler { request ->
                if (!apiMockEngineParams.encodedPath.isNullOrEmpty()) {
                    if (request.url.encodedPath == apiMockEngineParams.encodedPath) {
                        respond(
                            apiMockEngineParams.content.await(),
                            HttpStatusCode.OK,
                            apiMockEngineParams.headers
                        )
                    } else error("Unhandled ${request.url.encodedPath}")
                } else {
                    respond(
                        apiMockEngineParams.content.await(),
                        apiMockEngineParams.status,
                        apiMockEngineParams.headers
                    )
                }
            }
        }
    }
}