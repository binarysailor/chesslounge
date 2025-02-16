package net.binarysailor.chesslounge.client

import kotlinx.coroutines.runBlocking
import net.binarysailor.chesslounge.chesshouse.ChessHouse
import net.binarysailor.chesslounge.chesshouse.ChessHouseConfiguration
import net.binarysailor.chesslounge.chesshouse.api.stopChessHouseApi
import net.binarysailor.chesslounge.chesshouse.model.InMemoryPlayerRepository
import net.binarysailor.chesslounge.chesshouse.runChessHouse
import net.binarysailor.chesslounge.client.ChessLoungeClient.Handler
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import spark.Spark
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class ChessLoungeE2ETest {

    private lateinit var chessHouse: ChessHouse

    @Test
    fun `should receive seek play response`() {
        val testHandler = TestHandler()
        val client = createClient(testHandler)
        playerRepository.add("szymon")
        runBlocking {
            client.connect("szymon")
            client.seekPlay()
            await().atMost(500.milliseconds.toJavaDuration()).until {
                testHandler.seekResponseHasBeenReceived
            }
            client.disconnect()
        }
    }

    @Test
    fun `two players should receive game started when matched`() {
        val testHandler1 = TestHandler()
        val client1 = createClient(testHandler1)
        val testHandler2 = TestHandler()
        val client2 = createClient(testHandler2)
        playerRepository.add("zbigniew")
        playerRepository.add("jola")

        runBlocking {
            client1.connect("zbigniew")
            client1.seekPlay()
            client2.connect("jola")
            client2.seekPlay()

            await().atMost(1.seconds.toJavaDuration()).until {
                testHandler1.gameHasStarted && testHandler2.gameHasStarted
            }
        }
    }

    @Test
    fun `a move on each side is accepted`() {
        val testHandler1 = TestHandler()
        val client1 = createClient(testHandler1)
        val testHandler2 = TestHandler()
        val client2 = createClient(testHandler2)
        playerRepository.add("dominik")
        playerRepository.add("maria")

        runBlocking {
            client1.connect("dominik")
            client2.connect("maria")
            client1.seekPlay()
            client2.seekPlay()
        }

        await().until {
            testHandler1.gameHasStarted && testHandler2.gameHasStarted
        }

        val (white, black) = whoIsWho("dominik", client1, testHandler1, client2, testHandler2)

        runBlocking {
            white.client.move(testHandler1.game!!.id, "e2-e4")
        }
        await().atMost(Duration.ofMillis(300)).until {
            white.handler.moves.isNotEmpty() && black.handler.moves.isNotEmpty()
        }

        runBlocking {
            black.client.move(testHandler2.game!!.id, "e7-e5")
        }
        await().atMost(Duration.ofMillis(300)).until {
            white.handler.moves.equals(listOf(Move("e2-e4"), Move("e7-e5")))
        }
    }

    @Test
    fun `a move of valid player but with wrong piece side is not accepted`() {
        val testHandler1 = TestHandler()
        val client1 = createClient(testHandler1)
        val testHandler2 = TestHandler()
        val client2 = createClient(testHandler2)
        playerRepository.add("tomek")
        playerRepository.add("asia")

        runBlocking {
            client1.connect("asia")
            client2.connect("tomek")
            client1.seekPlay()
            client2.seekPlay()
        }

        await().until {
            testHandler1.gameHasStarted && testHandler2.gameHasStarted
        }

        val (white, black) = whoIsWho("asia", client1, testHandler1, client2, testHandler2)

        runBlocking {
            white.client.move(testHandler1.game!!.id, "e2-e4")
        }

        await().atMost(Duration.ofMillis(300)).until {
            white.handler.moves.isNotEmpty() && black.handler.moves.isNotEmpty()
        }

        runBlocking {
            black.client.move(testHandler1.game!!.id, "d2-d4")
        }

        await().atMost(Duration.ofMillis(300)).until {
            val lastMoveError = black.handler.lastMoveError
            lastMoveError?.move == Move("d2-d4")
                    && lastMoveError.code == "ILLEGAL"
                    && lastMoveError.message == "INVALID_PIECE_SIDE"
        }
    }

    @Test
    fun `a legal move sent by wrong side is not accepted`() {
        val testHandler1 = TestHandler()
        val client1 = createClient(testHandler1)
        val testHandler2 = TestHandler()
        val client2 = createClient(testHandler2)
        playerRepository.add("hubert")
        playerRepository.add("ewa")

        runBlocking {
            client1.connect("hubert")
            client2.connect("ewa")
            client1.seekPlay()
            client2.seekPlay()
        }

        await().until {
            testHandler1.gameHasStarted && testHandler2.gameHasStarted
        }

        val (white, black) = whoIsWho("hubert", client1, testHandler1, client2, testHandler2)

        runBlocking {
            white.client.move(testHandler1.game!!.id, "e2-e4")
        }

        await().atMost(Duration.ofMillis(300)).until {
            testHandler1.moves.isNotEmpty() && testHandler2.moves.isNotEmpty()
        }

        runBlocking {
            white.client.move(testHandler1.game!!.id, "e7-e6")
        }

        await().atMost(Duration.ofMillis(300)).until {
            val lastMoveError = white.handler.lastMoveError
            lastMoveError?.move == Move("e7-e6")
                    && lastMoveError.code == "ILLEGAL"
                    && lastMoveError.message == "OPPONENT_TURN"
        }
    }

    private fun createClient(testHandler: TestHandler): ChessLoungeClient {
        val port = config.port
        return ChessLoungeClient("http://localhost:$port", testHandler)
    }

    class TestHandler : Handler {

        var seekId: SeekID? = null
        var game: Game? = null
        var moves: MutableList<Move> = mutableListOf()
        var lastMoveError: MoveError? = null

        override fun seekResponseReceived(seekId: SeekID) {
            println("Seek response received $seekId")
            this.seekId = seekId
        }

        override fun gameStarted(gameId: GameID, white: Player, black: Player, seekId: SeekID?) {
            this.seekId = seekId
            this.game = Game(gameId, white, black)
        }

        override fun playerMoved(gameId: GameID, move: Move, newPosition: Position) {
            if (gameId != this.game?.id) {
                throw IllegalStateException()
            }
            moves += move
            lastMoveError = null
        }

        override fun moveAttempted(gameId: GameID, move: Move, code: String, message: String?) {
            if (gameId != this.game?.id) {
                throw IllegalStateException()
            }
            lastMoveError = MoveError(move, code, message)
        }

        val seekResponseHasBeenReceived
            get() = seekId != null

        val gameHasStarted
            get() = game != null

        data class MoveError(val move: Move, val code: String, val message: String?)
    }

    data class TestGameSide(val client: ChessLoungeClient, val handler: TestHandler)
    data class TestGameSides(val white: TestGameSide, val black: TestGameSide)
    fun whoIsWho(name1: String, client1: ChessLoungeClient, handler1: TestHandler,
                 client2: ChessLoungeClient, handler2: TestHandler): TestGameSides {
        if (handler1.game?.white?.name == name1) {
            return TestGameSides(white = TestGameSide(client1, handler1), black = TestGameSide(client2, handler2))
        } else {
            return TestGameSides(white = TestGameSide(client2, handler2), black = TestGameSide(client1, handler1))
        }
    }

    companion object {
        val playerRepository = InMemoryPlayerRepository()
        val config = ChessHouseConfiguration(port = 8130, playerRepositoryProvider =  { playerRepository })
        var chessHouse: ChessHouse? = null

        @JvmStatic
        @BeforeAll
        fun start() {
            println("Starting chesshouse")
            chessHouse = runChessHouse(config)
            Spark.awaitInitialization()
        }

        @JvmStatic
        @AfterAll
        fun stop() {
            stopChessHouseApi()
            Spark.awaitStop()
            println("Stopped chesshouse")
        }
    }
}