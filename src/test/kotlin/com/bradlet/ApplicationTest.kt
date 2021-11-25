package com.bradlet

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*

class ApplicationTest {

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