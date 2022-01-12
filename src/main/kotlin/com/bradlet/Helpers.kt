package com.bradlet

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.SendChannel


suspend fun SendChannel<Frame>.sendText(input: String) {
    send(Frame.Text(input))
}
