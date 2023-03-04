package com.kaizensundays.particles.fusion.mu

import com.kaizensundays.particles.fusion.mu.dao.FindFlightDao
import com.kaizensundays.particles.fusion.mu.dao.FindFlightLoader
import com.kaizensundays.particles.fusion.mu.dao.LogDao
import org.apache.ignite.Ignite
import org.apache.ignite.events.EventType
import org.h2.jdbcx.JdbcDataSource
import org.postgresql.ds.PGPoolingDataSource
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import javax.sql.DataSource

/**
 * Created: Saturday 9/25/2021, 1:44 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@Configuration
@EnableAutoConfiguration
@Import(IgniteContext::class)
open class ServiceContext {

    @Bean
    open fun dataSource(): DataSource {
        val ds = PGPoolingDataSource()
        ds.serverName = "PgSql"
        ds.portNumber = 30432
        ds.user = "postgres"
        ds.password = "postgres"
        return ds
    }

    @Bean
    open fun h2LogDataSource(): DataSource {
        val ds = JdbcDataSource()
        //ds.setURL("jdbc:h2:J:/super/projects/kaizensundays/fusion-alpha-2/fusion-mu/bin/h2/log")
        ds.setURL("jdbc:h2:J:/super/projects/kaizensundays/fusion-alpha-2/fusion-mu/bin/h2/log")
        ds.user = ""
        ds.password = ""
        return ds
    }

    @Bean
    open fun h2Jdbc(h2LogDataSource: DataSource) = NamedParameterJdbcTemplate(h2LogDataSource)

    @Bean
    open fun jdbc(dataSource: DataSource) = NamedParameterJdbcTemplate(dataSource)

    @Bean
    open fun h2LogDao(h2Jdbc: NamedParameterJdbcTemplate): LogDao {
        return LogDao(h2Jdbc)
    }

    @Bean
    open fun findFlightDao(jdbc: NamedParameterJdbcTemplate): FindFlightDao {
        return FindFlightDao(jdbc)
    }

    @Bean
    open fun findFlightHandler(ignite: Ignite): FindFlightHandler {
        return FindFlightHandler(ignite.getOrCreateCache(CacheName.Requests))
    }

    @Bean
    open fun frontEndWebSocketHandler(findFlightHandler: FindFlightHandler): FrontEndWebSocketHandler {
        return FrontEndWebSocketHandler(findFlightHandler)
    }

    @Bean
    open fun findFlightLoader(ignite: Ignite, dataSource: DataSource) = FindFlightLoader(ignite, dataSource)

    @Bean
    open fun handlerAdapter() = WebSocketHandlerAdapter()

    @Bean
    open fun handlerMapping(frontEndWebSocketHandler: FrontEndWebSocketHandler): SimpleUrlHandlerMapping {
        val map = mapOf(
            "/ws/frontend" to frontEndWebSocketHandler
        )

        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = map
        mapping.order = Ordered.HIGHEST_PRECEDENCE

        return mapping
    }

    @Bean
    open fun nodeState(ignite: Ignite, frontEndWebSocketHandler: FrontEndWebSocketHandler): NodeState {
        val nodeState = NodeState(ignite)
        nodeState.nodeStateListeners.add(frontEndWebSocketHandler)
        val events = ignite.events()
        events.localListen(nodeState, *EventType.EVTS_DISCOVERY)
        return nodeState
    }

    @Bean
    open fun defaultRestController(): DefaultRestController {
        return DefaultRestController()
    }

}