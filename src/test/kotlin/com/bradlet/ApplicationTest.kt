package com.bradlet

import com.bradlet.clients.EthereumClient
import com.bradlet.models.Declaration
import com.bradlet.models.GameState
import com.bradlet.models.StateChangeDeclaration
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.web3j.boardcrypetition.BoardCrypetition
import org.web3j.protocol.core.RemoteFunctionCall
import java.math.BigInteger

class ApplicationTest {

    private val contractMock = mockk<BoardCrypetition>(relaxed = true)
    private val ethClient = EthereumClient(
        contractMock,
        mockk(relaxed = true),
        mockk(relaxed = true),
        mockk(relaxed = true)
    )

    @BeforeEach
    fun setupTests() {
        // RemoteFunctionCall makes mocking a lil' annoying...

        // Used in Game route
        every { contractMock.findGameLobby(any()) } returns
                mockRemoteFunctionCall<SolLobbyTuple>(EXAMPLE_GAME_LOBBY_SOL)
        every { contractMock.lookupGameState(any()) } returns
                mockRemoteFunctionCall<BigInteger>(
                    BigInteger.valueOf(GameState.READY.stateCode.toLong())
                )
        // Used in BasePath route
        every { contractMock.recentOpenLobbies } returns
                mockRemoteFunctionCall<List<*>>(EXAMPLE_LOBBY_LIST)
    }

    @AfterEach
    fun cleanupTests() {
        // Clean up all mocks after each tests so that test states don't clash.
        clearAllMocks()
    }

    @Test
    fun `redirects standard http traffic to https`() {
        withTestApplication({
            mainApp(ethClient)
        }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.MovedPermanently, response.status())
            }
        }
    }

    @Test
    fun `responds with OK and array of GameLobby objects`() {
        withHttpsTestApplication({
            mainApp(ethClient)
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
            val lobbies = Gson().fromJson(response.content, Array<BigInteger>::class.java)
            assertArrayEquals(EXAMPLE_LOBBY_LIST.toTypedArray(), lobbies)
        }
    }

    @Test
    fun `Server greets new connections`() {
        withHttpsWebSocketConversation(
            moduleFunction = { mainApp(ethClient) },
            uri = "/game/1"
        ) { incoming, outgoing ->
            // Test greeting
            val greeting = (incoming.receive() as Frame.Text).readText()
            assert(greeting.contains("Joined lobby: ${EXAMPLE_GAME_LOBBY.gameId}"))
        }
    }

    @Test
    fun `Server handles StateChangeDeclaration`() {
        withHttpsWebSocketConversation(
            moduleFunction = { mainApp(ethClient) },
            uri = "/game/1"
        ) { incoming, outgoing ->
            incoming.receive() // throw out the initial greeting, not needed for this test

            outgoing.send(
                Frame.Text(
                    Gson().toJson(
                        StateChangeDeclaration(
                            playerAddress = AN_ADDRESS,
                            declaration = Declaration.WON
                        )
                    )
                )
            )
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

inline fun <reified T> mockRemoteFunctionCall(toReturn: Any): RemoteFunctionCall<T> {
    val mock = mockk<RemoteFunctionCall<T>>()
    every { mock.send() } returns toReturn as T
    return mock
}