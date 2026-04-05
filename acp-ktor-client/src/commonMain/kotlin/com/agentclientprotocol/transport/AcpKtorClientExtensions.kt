package com.agentclientprotocol.transport

import com.agentclientprotocol.protocol.Protocol
import com.agentclientprotocol.protocol.ProtocolOptions
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.HttpRequestBuilder

/**
 * Create a new [Protocol] on a websocket via [HttpClient].
 *
 * The protocol should be started manually by the calling site.
 *
 * @param onClose optional callback invoked when the underlying WebSocket transport closes
 *   (including unexpected closes due to network loss). Use this to detect disconnections and
 *   drive reconnection logic on the caller side.
 */
public suspend fun HttpClient.acpProtocolOnClientWebSocket(
    url: String = ACP_PATH,
    protocolOptions: ProtocolOptions,
    onClose: (() -> Unit)? = null,
    requestBuilder: HttpRequestBuilder.() -> Unit = {}
): Protocol {
    val webSocketSession = webSocketSession(urlString = url, block = requestBuilder)
    val webSocketTransport = WebSocketTransport(parentScope = webSocketSession, wss = webSocketSession)
    onClose?.let { webSocketTransport.onClose { it() } }
    val protocol = Protocol(parentScope = webSocketSession, transport = webSocketTransport, options = protocolOptions)
    return protocol
}