package net.binarysailor.chesslounge.chesshouse.testutil

import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.UpgradeRequest
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

object WebSocketSessions {
    fun aSession(init: Builder.() -> Unit = {}): Session {
        val builder = Builder()
        builder.init()
        return builder.build()
    }
}

class Builder {
    private var headers = mutableMapOf<String, String>()
    fun header(name: String, value: String) = headers::put

    fun build(): Session {
        val remoteEndpoint = mock<RemoteEndpoint> {}
        val upgradeReq = mock<UpgradeRequest> {
            on { getHeader(any()) }.doAnswer { headers[it.arguments[0]] }
        }

        return mock<Session> {
            on { remote } doReturn remoteEndpoint
            on { upgradeRequest } doReturn upgradeReq
        }
    }
}