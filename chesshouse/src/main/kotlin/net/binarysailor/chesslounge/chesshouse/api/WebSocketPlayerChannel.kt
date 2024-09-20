package net.binarysailor.chesslounge.chesshouse.api

import com.google.gson.Gson
import net.binarysailor.chesslounge.chesshouse.PlayerChannel
import net.binarysailor.chesslounge.chesshouse.messages.InstantMessage
import org.eclipse.jetty.websocket.api.Session

internal class WebSocketPlayerChannel(private val session: Session): PlayerChannel {
    private companion object {
        private val gson = Gson()
    }

    override fun send(message: InstantMessage) {
        session.remote.sendString(message.asJson())
    }

    private fun InstantMessage.asJson() = gson.toJson(this)
}