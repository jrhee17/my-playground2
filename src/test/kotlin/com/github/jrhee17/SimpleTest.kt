package com.github.jrhee17

import com.linecorp.armeria.client.websocket.WebSocketClient
import com.linecorp.armeria.common.SerializationFormat
import com.linecorp.armeria.common.SessionProtocol
import com.linecorp.armeria.common.websocket.WebSocketFrame
import com.linecorp.armeria.common.websocket.WebSocketFrameType
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.graphql.GraphqlService
import com.linecorp.armeria.server.graphql.RuntimeWiringConfigurator
import com.linecorp.armeria.testing.junit5.server.ServerExtension
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.reactive.asPublisher
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.io.File

class SimpleTest {

    companion object {
        @JvmField
        @RegisterExtension
        val server: ServerExtension =
            object : ServerExtension() {
                @Throws(Exception::class)
                override fun configure(sb: ServerBuilder) {
                    configureService(sb)
                }
            }

        fun configureService(sb: ServerBuilder) {
            val graphqlSchemaFile = File({}.javaClass.getResource("/graphql/subscription.graphqls")!!.toURI())
            val service = GraphqlService.builder()
                .schemaFile(graphqlSchemaFile)
                .enableWebSocket(true)
                .runtimeWiring(RuntimeWiringConfigurator { c: RuntimeWiring.Builder ->
                    c.type("Subscription") { typeWiring: TypeRuntimeWiring.Builder ->
                        typeWiring.dataFetcher(
                            "hello",
                            clientInitiated()
                        )
                    }
                    c.type("Query") { typeWiring: TypeRuntimeWiring.Builder ->
                        typeWiring.dataFetcher(
                            "foo",
                            neverClosing()
                        )
                    }
                })
                .build()
            sb.service("/graphql", service)
        }

        private fun neverClosing(): DataFetcher<Publisher<String>> {
            return DataFetcher { environment: DataFetchingEnvironment? ->
                val flow = callbackFlow {
                    channel.trySend("Armeria")
                    channel.close()
                    awaitClose {
                        print("Closing!")
                    }
                }
                flow.asPublisher()
            }
        }

        private fun clientInitiated(): DataFetcher<Publisher<String>> {
            return DataFetcher { environment: DataFetchingEnvironment? ->
                val flow = callbackFlow {
                    channel.trySend("Armeria")
                    channel.close()
                    awaitClose {
                        print("Closing!")
                    }
                }
                flow.asPublisher()
            }
        }
    }

//    @Test
//    fun testPublisherClosed() {
//        val webSocketClient: WebSocketClient = WebSocketClient.builder(
//            server.uri(
//                SessionProtocol.H2C,
//                SerializationFormat.WS
//            )
//        )
//            .subprotocols("graphql-transport-ws")
//            .build()
//        val future = webSocketClient.connect("/graphql")
//
//        val webSocketSession = future.join()
//
//        val outbound = webSocketSession.outbound()
//
//        val receivedEvents: MutableList<String> = ArrayList()
//        //noinspection ReactiveStreamsSubscriberImplementation
//        webSocketSession.inbound().subscribe(object : Subscriber<WebSocketFrame> {
//            override fun onSubscribe(s: Subscription) {
//                s.request(Long.MAX_VALUE)
//            }
//
//            override fun onNext(webSocketFrame: WebSocketFrame) {
//                if (webSocketFrame.type() == WebSocketFrameType.TEXT) {
//                    receivedEvents.add(webSocketFrame.text())
//                }
//            }
//
//            override fun onError(t: Throwable) {}
//            override fun onComplete() {}
//        })
//
//        outbound.write("{\"type\":\"ping\"}")
//        outbound.write("{\"type\":\"connection_init\"}")
//        outbound.write(
//            "{\"id\":\"1\",\"type\":\"subscribe\",\"payload\":{\"query\":\"subscription {hello}\"}}"
//        )
//
//        await().untilAsserted {
//            assertThat(receivedEvents).hasSize(4)
//        }
//        print(receivedEvents)
//        assertThatJson(receivedEvents[0]).node("type").isEqualTo("pong")
//        assertThatJson(receivedEvents[1]).node("type").isEqualTo("connection_ack")
//        assertThatJson(receivedEvents[2])
//            .node("type").isEqualTo("next")
//            .node("id").isEqualTo("\"1\"")
//            .node("payload.data.hello").isEqualTo("Armeria")
//        assertThatJson(receivedEvents[3])
//            .node("type").isEqualTo("complete")
//            .node("id").isEqualTo("\"1\"")
//    }

    @Test
    fun testConnectionClosed() {
        val webSocketClient: WebSocketClient = WebSocketClient.builder(
            server.uri(
                SessionProtocol.H2C,
                SerializationFormat.WS
            )
        )
            .subprotocols("graphql-transport-ws")
            .build()
        val future = webSocketClient.connect("/graphql")

        val webSocketSession = future.join()

        val outbound = webSocketSession.outbound()

        val receivedEvents: MutableList<String> = ArrayList()
        //noinspection ReactiveStreamsSubscriberImplementation
        webSocketSession.inbound().subscribe(object : Subscriber<WebSocketFrame> {
            override fun onSubscribe(s: Subscription) {
                s.request(Long.MAX_VALUE)
            }

            override fun onNext(webSocketFrame: WebSocketFrame) {
                if (webSocketFrame.type() == WebSocketFrameType.TEXT) {
                    receivedEvents.add(webSocketFrame.text())
                }
            }

            override fun onError(t: Throwable) {}
            override fun onComplete() {}
        })

        outbound.write("{\"type\":\"ping\"}")
        outbound.write("{\"type\":\"connection_init\"}")
        outbound.write(
            "{\"id\":\"1\",\"type\":\"subscribe\",\"payload\":{\"query\":\"query {foo}\"}}"
        )

        await().untilAsserted {
            assertThat(receivedEvents).hasSize(4)
        }
        print(receivedEvents)
        assertThatJson(receivedEvents[0]).node("type").isEqualTo("pong")
        assertThatJson(receivedEvents[1]).node("type").isEqualTo("connection_ack")
        assertThatJson(receivedEvents[2])
            .node("type").isEqualTo("next")
            .node("id").isEqualTo("\"1\"")
            .node("payload.data.hello").isEqualTo("Armeria")
        assertThatJson(receivedEvents[3])
            .node("type").isEqualTo("complete")
            .node("id").isEqualTo("\"1\"")
    }
}