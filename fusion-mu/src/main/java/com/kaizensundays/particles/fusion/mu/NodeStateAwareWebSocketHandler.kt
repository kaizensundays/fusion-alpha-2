package com.kaizensundays.particles.fusion.mu

import org.reactivestreams.Subscription
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created: Sunday 2/26/2023, 11:13 AM Eastern Time
 *
 * @author Sergey Chuykov
 */
abstract class NodeStateAwareWebSocketHandler : WebSocketHandler, NodeStateListener {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val active = AtomicBoolean(false)

    private val sessionMap = mutableMapOf<String, WebSocketSession>()

    protected fun addSession(session: WebSocketSession, subscription: Subscription) {
        if (active.get()) {
            sessionMap[session.id] = session
        } else {
            subscription.cancel()
        }
    }

    protected fun removeSession(session: WebSocketSession) {
        sessionMap.remove(session.id)
        logger.info("Session ${session.id} is removed and closed")
    }

    fun closeSession(session: WebSocketSession) {
        try {
            logger.info("Closing session: ${session.id}")
            session.close(CloseStatus.GOING_AWAY)
                .block(Duration.ofSeconds(10))
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
    }

    fun closeSessions() {
        sessionMap.keys.toList()
            .mapNotNull { sessionId -> sessionMap[sessionId] }
            .forEach { session -> closeSession(session) }
    }

    override fun onStateChange(active: Boolean) {
        this.active.set(active)

        if (!active) {
            closeSessions()
        }
    }

}