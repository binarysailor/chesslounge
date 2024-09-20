package net.binarysailor.chesslounge.client

import kotlinx.coroutines.runBlocking
import net.binarysailor.chesslounge.chesshouse.ChessHouseConfiguration
import net.binarysailor.chesslounge.chesshouse.api.stopChessHouseApi
import net.binarysailor.chesslounge.chesshouse.runChessHouse
import net.binarysailor.chesslounge.client.ChessLoungeClient.Handler
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import spark.Spark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class ChessLoungeClientTest {

    @Test
    fun `should receive seek play response`() {
        val testHandler = TestHandler()
        val client = createClient(testHandler)
        runBlocking {
            client.connect("tomek")
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
        runBlocking {
            client1.connect("tomek")
            client1.seekPlay()
            client2.connect("andrzej")
            client2.seekPlay()

            await().atMost(1.seconds.toJavaDuration()).until {
                testHandler1.gameHasStarted && testHandler2.gameHasStarted
            }
        }
    }

    @BeforeEach
    fun start() {
        runChessHouse(config)
        Spark.awaitInitialization()
    }

    @AfterEach
    fun stop() {
        stopChessHouseApi()
        Spark.awaitStop()
    }

    private fun createClient(testHandler: TestHandler): ChessLoungeClient {
        val port = config.port
        return ChessLoungeClient("http://localhost:$port", testHandler)
    }

    class TestHandler : Handler {

        var seekId: SeekID? = null
        var gameId: GameID? = null
        override fun seekResponseReceived(id: SeekID) {
            println("Seek response received $id")
            seekId = id
        }

        override fun gameStarted(seekId: SeekID, gameId: GameID) {
            this.seekId = seekId
            this.gameId = gameId
        }

        val seekResponseHasBeenReceived
            get() = seekId != null

        val gameHasStarted
            get() = gameId != null
    }


    companion object {
        val config = ChessHouseConfiguration(port = 8130)
    }
}