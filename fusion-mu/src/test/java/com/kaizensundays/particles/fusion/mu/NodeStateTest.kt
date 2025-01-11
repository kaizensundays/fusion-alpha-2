package com.kaizensundays.particles.fusion.mu

import org.apache.ignite.Ignite
import org.apache.ignite.spi.discovery.tcp.internal.TcpDiscoveryNode
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Created: Saturday 2/25/2023, 11:16 AM Eastern Time
 *
 * @author Sergey Chuykov
 */
class NodeStateTest {

    private val ignite: Ignite = mock()

    private val state = NodeState(ignite)

    private fun node(votes: Int, quorum: Int): TcpDiscoveryNode {
        val node = TcpDiscoveryNode(
            UUID.randomUUID(),
            emptyList<String>(), emptyList<String>(),
            0, mock(), mock(), ""
        )
        node.attributes = mapOf(
            NodeState.CLUSTER_VOTES_PROP to votes.toString(),
            NodeState.CLUSTER_QUORUM_PROP to quorum.toString(),
        )
        return node
    }

    @Test
    fun isActive() {

        assertFalse(state.isActive(listOf(node(0, 0))))
        assertFalse(state.isActive(listOf(node(1, 0))))

        assertTrue(state.isActive(listOf(node(1, 1))))

        assertTrue(state.isActive(listOf(node(1, 2), node(1, 2), node(1, 2))))
        assertTrue(state.isActive(listOf(node(1, 2), node(1, 2))))
        assertFalse(state.isActive(listOf(node(1, 2))))

        assertTrue(state.isActive(listOf(node(2, 2), node(1, 2))))
        assertTrue(state.isActive(listOf(node(2, 2))))

        // quorum values are not equal
        assertThrows(IllegalArgumentException::class.java) {
            state.isActive(listOf(node(1, 2), node(1, 1)))
        }
    }

}