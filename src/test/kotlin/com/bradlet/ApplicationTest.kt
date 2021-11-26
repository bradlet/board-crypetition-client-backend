package com.bradlet

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.mockk
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.web3j.tx.Contract


class ApplicationTest {

    val contractMock: Contract = mockk()

    @AfterEach
    fun cleanupTests() {
        // Clean up all mocks after each tests so that test states don't clash.
        clearAllMocks()
    }

    @Test
    fun `Redirects standard http traffic to https`() {
        withTestApplication(Application::mainApp) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.MovedPermanently, response.status())
            }
        }
    }

    @Test
    fun `Server runs and responds okay to https traffic`() {
        withHttpsTestApplication(Application::mainApp) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("Hello World!", response.content)
        }
    }

    @Test
    fun `Server handles basic WebSocket conversation`() {
        withHttpsWebSocketConversation(
            moduleFunction = Application::mainApp,
            uri = "/game"
        ) { incoming, outgoing ->
            outgoing.send(Frame.Text("test"))
            val response = (incoming.receive() as Frame.Text).readText()
            assertEquals(response, "YOU SAID: test")
        }
    }
}

/**
 * Helpful wrapper which handles test https redirection setup to cleanup
 * tests. The HttpsRedirect plugin would otherwise cause a 301 response on all
 * tests.
 */
private fun withHttpsTestApplication(
    moduleFunction: Application.() -> Unit,
    httpMethod: HttpMethod = HttpMethod.Get,
    uri: String = "/",
    assertions: TestApplicationCall.() -> Unit
) {
    withTestApplication({
        install(XForwardedHeaderSupport)
        moduleFunction()
    }) {
        handleRequest(httpMethod, uri, setup = {
            addHeader(HttpHeaders.XForwardedProto, "https")
        }).apply { assertions() }
    }
}

/**
 * Similar wrapper to `withHttpsTestApplication` but for WebSockets
 */
private fun withHttpsWebSocketConversation(
    moduleFunction: Application.() -> Unit,
    uri: String,
    conversation: suspend TestApplicationCall.(ReceiveChannel<Frame>, SendChannel<Frame>) -> Unit
) {
    withTestApplication({
        install(XForwardedHeaderSupport)
        moduleFunction()
    }) {
       handleWebSocketConversation(
           uri = uri,
           setup = { addHeader(HttpHeaders.XForwardedProto, "https") },
           callback = conversation
       )
    }
}