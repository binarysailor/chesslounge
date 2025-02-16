package net.binarysailor.chesslounge.chesshouse.api.authentication

import net.binarysailor.chesslounge.chesshouse.model.Player
import net.binarysailor.chesslounge.chesshouse.model.PlayerRepository
import org.eclipse.jetty.websocket.api.UpgradeRequest
import spark.Request

internal class TrivialAuthenticator(private val repository: PlayerRepository) : Authenticator {
    override fun authenticatePlayer(request: Request): Player {
        return playerByAuthHeader(request.headers("Authorization"))
    }

    override fun authenticatePlayer(request: UpgradeRequest): Player {
        return playerByAuthHeader(request.getHeader("Authorization"))
    }

    private fun playerByAuthHeader(userName: String) = repository.findPlayerByName(userName) ?: throw AuthenticationException()
}