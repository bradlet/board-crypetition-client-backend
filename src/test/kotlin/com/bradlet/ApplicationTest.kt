package com.bradlet

import com.bradlet.clients.EthereumClient
import com.bradlet.models.GameLobby
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApplicationTest {

    private val clientMock = mockk<EthereumClient>(relaxed = true)

    @BeforeEach
    fun setupTests() {
        // using relaxed mocking to get around EthHttpClient setup, but still want the returns
        // from my temp mocking extension functions
        every { clientMock.getAllGameLobbies() } answers { callOriginal() }
        coEvery { clientMock.findGameLobby(any()) } coAnswers { callOriginal() }
    }

    @AfterEach
    fun cleanupTests() {
        // Clean up all mocks after each tests so that test states don't clash.
        clearAllMocks()
    }

    @Test
    fun `redirects standard http traffic to https`() {
        withTestApplication({
            mainApp(clientMock)
        }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.MovedPermanently, response.status())
            }
        }
    }

    @Test
    fun `responds with OK and array of GameLobby objects`() {
        withHttpsTestApplication({
            mainApp(clientMock)
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
            val lobbies = Gson().fromJson(response.content, Array<GameLobby>::class.java)
            assertNotEquals(arrayOf<GameLobby>(), lobbies)
            // Since the mock function getAllGameLobbies currently returns the same GameLobby object,
            // this has the same gameId everytime... this test doesn't provide value, just setting up the structure.
            assertEquals(lobbies.map { "1" }, lobbies.map { it.gameId })
        }
    }

    @Test
    fun `Server handles basic WebSocket conversation`() {
        withHttpsWebSocketConversation(
            moduleFunction = { mainApp(clientMock) },
            uri = "/game/game1"
        ) { incoming, outgoing ->
            // Test greeting
            val greeting = (incoming.receive() as Frame.Text).readText()
            assert(greeting.contains("Joined lobby: ${TEMPORARY_GAME_LOBBY.gameId}"))

            // Test simple conversation
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