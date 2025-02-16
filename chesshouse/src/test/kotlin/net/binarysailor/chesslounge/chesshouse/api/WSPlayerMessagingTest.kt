package net.binarysailor.chesslounge.chesshouse.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.binarysailor.chesslounge.chesshouse.GameMatcher.SeekID
import net.binarysailor.chesslounge.chesshouse.model.GameID
import net.binarysailor.chesslounge.chesshouse.model.Player
import net.binarysailor.chesslounge.chesshouse.model.PlayerID
import net.binarysailor.chesslounge.chesshouse.model.messaging.GameStartedMessage
import net.binarysailor.chesslounge.chesshouse.model.messaging.MessageId
import net.binarysailor.chesslounge.chesshouse.model.messaging.SeekResponseMessage
import net.binarysailor.chesslounge.chesshouse.testutil.WebSocketSessions.aSession
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.websocket.api.RemoteEndpoint
import org.mockito.kotlin.argumentCaptor
import java.util.*
import java.util.UUID.randomUUID
import java.util.function.Consumer
import kotlin.test.Test

class WSPlayerMessagingTest {
    val messaging = WSPlayerMessaging()

    @Test
    fun `sends correct message with correlationId`() {
        // given
        val johnny = Player(PlayerID(randomUUID()), "johnny")
        val monica = Player(PlayerID(randomUUID()), "monica")
        val session = aSession()
        val gameId = GameID(randomUUID())
        val correlationId = MessageId(randomUUID())
        messaging.addPlayer(johnny, session)

        // when
        messaging.messagePlayer(johnny.id, GameStartedMessage(gameId, johnny, monica), correlationId)

        // then
        verify(session.remote).receivedJsonWith() {
            field("gameId") equalTo gameId.id
            field("correlationId") equalTo correlationId.id
        }
    }

    @Test
    fun `sends correct message without correlationId`() {
        // given
        val johnny = Player(PlayerID(randomUUID()), "johnny")
        val session = aSession()
        val seekId = SeekID(randomUUID())
        messaging.addPlayer(johnny, session)

        // when
        messaging.messagePlayer(johnny.id, SeekResponseMessage(seekId, true, "some message"), null)

        // then
        verify(session.remote).receivedJsonWith {
            field("seekId") equalTo seekId.id
            field("ok") equalTo true
            field("correlationId").notFound()
        }
    }
}

fun verify(remoteEndpoint: RemoteEndpoint) = RemoteEndpointAssert(remoteEndpoint)

class RemoteEndpointAssert(private val endpoint: RemoteEndpoint) {
    fun receivedJsonWith(jsonAssertions: JsonAssert.() -> Unit) {
        argumentCaptor<String>().apply {
            org.mockito.kotlin.verify(endpoint).sendString(capture())

            val jsonTree = JsonParser.parseString(firstValue) as JsonObject
            val jsonAssert = JsonAssert(jsonTree)
            jsonAssert.assertAll(jsonAssertions)
        }
    }
}

class JsonAssert(private val json: JsonObject) {

    private val assertions: MutableList<Consumer<JsonObject>> = mutableListOf()
    fun assertAll(assertionSetup: JsonAssert.() -> Unit) {
        this.assertionSetup()
        assertions.forEach { it.accept(json) }
    }

    fun field(name: String) = JsonFieldAssert(name, assertions)
}

class JsonFieldAssert(private val fieldName: String, private val target: MutableList<Consumer<JsonObject>>) {

    infix fun equalTo(expected: UUID) {
        target.add {
            assertThat(it.get(fieldName).asString).isEqualTo(expected.toString())
        }
    }

    infix fun equalTo(expected: Boolean) {
        target.add {
            assertThat(it.get(fieldName).asBoolean).isEqualTo(expected)
        }
    }

    fun notFound() {
        target.add {
            assertThat(it.has(fieldName)).isFalse()
        }
    }
}