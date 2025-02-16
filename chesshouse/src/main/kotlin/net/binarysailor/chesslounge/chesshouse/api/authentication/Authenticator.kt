package net.binarysailor.chesslounge.chesshouse.api.authentication

import net.binarysailor.chesslounge.chesshouse.model.Player
import org.eclipse.jetty.websocket.api.UpgradeRequest
import spark.Request

interface Authenticator {
    fun authenticatePlayer(request: Request): Player
    fun authenticatePlayer(request: UpgradeRequest): Player
}

class AuthenticationException : Exception() {
}
