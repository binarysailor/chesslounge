package net.binarysailor.chesslounge.chesshouse.api

import net.binarysailor.chesslounge.chesshouse.ChessHouse
import net.binarysailor.chesslounge.chesshouse.ChessHouse.MakeMoveResult
import net.binarysailor.chesslounge.chesshouse.GameMatcher
import net.binarysailor.chesslounge.chesshouse.api.authentication.Authenticator
import net.binarysailor.chesslounge.chesshouse.model.FENPosition
import net.binarysailor.chesslounge.chesshouse.model.GameID
import net.binarysailor.chesslounge.chesshouse.model.Player
import net.binarysailor.chesslounge.chesshouse.model.PlayerID
import net.binarysailor.chesslounge.chesshouse.model.messaging.MessageId
import net.binarysailor.chesslounge.chesshouse.model.messaging.MoveResponseMessage
import net.binarysailor.chesslounge.chesshouse.model.messaging.MoveResponseMessage.MoveDetails
import net.binarysailor.chesslounge.chesshouse.model.messaging.PlayerMessage
import net.binarysailor.chesslounge.chesshouse.testutil.WebSocketSessions.aSession
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.websocket.api.UpgradeRequest
import org.mockito.kotlin.*
import java.util.*
import java.util.UUID.randomUUID
import kotlin.test.Test

class ChessHouseWSHandlerTest {
    private val authenticator = mock<Authenticator>()
    private val chessHouse = mock<ChessHouse>()
    private val playerMessaging = spy(WSPlayerMessaging())
    private val gameMatcher = spy(GameMatcher(chessHouse, playerMessaging))

    private val handler = ChessHouseWSHandler(authenticator, chessHouse, gameMatcher, playerMessaging)

    @Test
    fun `on connect adds player to chesshouse`() {
        val session = aSession()
        val player = Player(PlayerID(randomUUID()), "john")
        whenever(authenticator.authenticatePlayer(any<UpgradeRequest>())).thenReturn(player)

        handler.onConnect(session)

        verify(chessHouse).addPlayer(player)
    }

    @Test
    fun `on connect adds player to messaging`() {
        val session = aSession()
        val player = Player(PlayerID(randomUUID()), "john")
        whenever(authenticator.authenticatePlayer(any<UpgradeRequest>())).thenReturn(player)

        handler.onConnect(session)

        assertThat(playerMessaging.hasPlayer(player)).isTrue()
    }

    @Test
    fun `on disconnect removes player from chesshouse`() {
        val session = aSession()
        val player = Player(PlayerID(randomUUID()), "john")
        whenever(authenticator.authenticatePlayer(any<UpgradeRequest>())).thenReturn(player)

        handler.onConnect(session)
        handler.onClose(session, 0, "Some reason")

        assertThat(chessHouse.findPlayer("john")).isNull()
    }

    @Test
    fun `on disconnect removes player from messaging`() {
        val session = aSession()
        val player = Player(PlayerID(randomUUID()), "john")
        whenever(authenticator.authenticatePlayer(any<UpgradeRequest>())).thenReturn(player)

        handler.onConnect(session)
        handler.onClose(session, 0, "Some reason")

        assertThat(playerMessaging.hasPlayer(player)).isFalse()
    }

    @Test
    fun `on command seekplay from previously connected session adds seeker to game matcher`() {
        val session = aSession()
        val player = Player(PlayerID(randomUUID()), "john")
        whenever(authenticator.authenticatePlayer(any<UpgradeRequest>())).thenReturn(player)

        handler.onConnect(session)
        handler.onMessage(session, """
            {
            "messageId": "363f1ec1-bd67-47b5-8325-44752493787d",
            "message": "seekplay"
            }
            """.trimIndent())

        verify(gameMatcher).addSeeker(player)
    }

    @Test
    fun `on command seekplay from unconnected session does not add a seeker`() {
        val session = aSession()
        val player = Player(PlayerID(randomUUID()), "john")
        whenever(authenticator.authenticatePlayer(any<UpgradeRequest>())).thenReturn(player)

        handler.onMessage(session, """
            {
            "messageId": "363f1ec1-bd67-47b5-8325-44752493787d" ,
            "message": "seekplay"
            }
            """.trimIndent())

        verifyNoInteractions(gameMatcher)
    }

    @Test
    fun `on command game move from previously connected session makes a move`() {

        val session = aSession()
        val player = Player(PlayerID(randomUUID()), "john")
        whenever(authenticator.authenticatePlayer(any<UpgradeRequest>())).thenReturn(player)
        whenever(chessHouse.makeMove(any<Player>(), any<GameID>(), any<String>()))
            .thenAnswer {
                val invocationPlayer: Player = it.getArgument(0)
                val invocationGameId = GameID(it.getArgument(1))
                val invocationMoveSymbol: String = it.getArgument(2)

                return@thenAnswer MakeMoveResult(
                    listOf(
                        PlayerMessage(
                            player = invocationPlayer,
                            message = MoveResponseMessage(
                                gameId = invocationGameId,
                                symbol = invocationMoveSymbol,
                                success = MoveDetails(
                                    number = 1,
                                    newPosition = FENPosition("XYZ")
                                )
                            )
                        )
                    )
                )
            }

        handler.onConnect(session)
        handler.onMessage(session, """
            {
            "messageId": "363f1ec1-bd67-47b5-8325-44752493787d", 
            "message": "game eefbdd27-2303-4ee7-8c6f-fd4a47154299 move e2-e4"
            }
            """.trimIndent())

        verify(chessHouse).makeMove(player, GameID("eefbdd27-2303-4ee7-8c6f-fd4a47154299".uuid()), "e2-e4")
        verify(playerMessaging).messagePlayer(player.id, MoveResponseMessage(
            gameId = GameID("eefbdd27-2303-4ee7-8c6f-fd4a47154299".uuid()),
            symbol = "e2-e4",
            success = MoveDetails(
                number = 1,
                newPosition = FENPosition("XYZ")
            )
        ), MessageId("363f1ec1-bd67-47b5-8325-44752493787d".uuid()))
    }

    private fun String.uuid() = UUID.fromString(this)
}