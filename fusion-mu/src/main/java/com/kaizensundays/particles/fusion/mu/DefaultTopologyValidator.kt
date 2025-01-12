package com.kaizensundays.particles.fusion.mu

import org.apache.ignite.cluster.ClusterNode
import org.apache.ignite.configuration.TopologyValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created: Monday 2/20/2023, 11:16 AM Eastern Time
 *
 * @author Sergey Chuykov
 */
class DefaultTopologyValidator : TopologyValidator {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(DefaultTopologyValidator::class.java)
    }

    override fun validate(nodes: MutableCollection<ClusterNode>): Boolean {

        var quorum = 1000
        var votes = 0

        nodes.forEach { node ->
            val clusterQuorum = node.attribute<String>("cluster.quorum")?.toInt() ?: 0
            quorum = clusterQuorum
            val clusterVotes = node.attribute<String>("cluster.votes")?.toInt() ?: 0
            votes += clusterVotes
            val id = node.id().toString()
            logger.info("nodeId=$id clusterQuorum=$clusterQuorum clusterVotes=$clusterVotes")
        }

        val isQuorum = votes >= quorum
        logger.info("isQuorum=$isQuorum")

        return isQuorum
    }

}