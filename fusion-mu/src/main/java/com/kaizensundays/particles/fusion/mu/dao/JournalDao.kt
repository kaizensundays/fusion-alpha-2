package com.kaizensundays.particles.fusion.mu.dao

import com.kaizensundays.particles.fusion.mu.messages.Journal
import com.kaizensundays.particles.fusion.mu.messages.JournalState
import org.h2.jdbc.JdbcResultSet
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*

/**
 * Created: Saturday 3/4/2023, 11:56 AM Eastern Time
 *
 * @author Sergey Chuykov
 */
class JournalDao(private val jdbc: NamedParameterJdbcTemplate) : RowMapper<Journal> {

    override fun mapRow(rs: ResultSet, rowNum: Int): Journal {
        val h2rs = rs as JdbcResultSet
        return Journal(
            rs.getLong("id"),
            rs.getInt("state"),
            h2rs.getTimestamp("time"),
            rs.getString("uuid"),
            rs.getString("msg")
        )
    }

    fun tableExist(tableName: String): Boolean {
        return jdbc.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_name = :tableName)",
            mapOf("tableName" to tableName.uppercase()), Boolean::class.java
        ) ?: false
    }

    fun createTable(): Int {
        return jdbc.update(
            "CREATE TABLE IF NOT EXISTS journal (" +
                    " ID BIGINT auto_increment PRIMARY KEY, STATE INT," +
                    " TIME TIMESTAMP WITH TIME ZONE," +
                    " UUID VARCHAR(36)," +
                    " MSG VARCHAR(1000)" +
                    ")",
            mapOf<String, Any>()
        )
    }

    fun createTableIfItDoesNotExist(): Boolean {
        val existed = tableExist("journal")
        if (!existed) {
            createTable()
        }
        return existed
    }

    fun findById(id: Long): Journal? {
        return jdbc.queryForObject("select * from journal where id=:id", mapOf("id" to id), this)
    }

    fun findAll(): List<Journal> {
        return jdbc.query("select * from journal order by id desc", this)
    }

    fun findByUUID(uuid: String): Journal? {
        return jdbc.queryForObject("select * from journal where uuid=:uuid", mapOf("uuid" to uuid), this)
    }

    fun findByState(state: Int): List<Journal> {
        return jdbc.query("select * from journal where state=:state order by id", mapOf("state" to state), this)
    }

    fun findBefore(time: Date, state: JournalState): List<Journal> {
        val params = mapOf("time" to time, "state" to state.value)
        return jdbc.query("select * from journal where time < :time and state=:state", params, this)
    }

    fun truncate(): Int {
        return jdbc.update("truncate table journal", mapOf<String, Any>())
    }

    fun insert(journal: Journal): Int {
        return jdbc.update(
            "insert into JOURNAL (STATE,TIME, UUID, MSG) VALUES (:state, :time, :uuid, :msg)",
            mapOf(
                "state" to journal.state, "time" to journal.time, "uuid" to journal.uuid, "msg" to journal.msg,
                "x" to "X"
            )
        )
    }

    fun updateStateById(id: Long, state: JournalState) {
        jdbc.update("update JOURNAL set STATE=:state where ID=:id", mapOf("id" to id, "state" to state.value))
    }

    fun updateStateByUUID(uuid: String, state: JournalState): Int {
        return jdbc.update("update JOURNAL set STATE=:state where UUID=:uuid", mapOf("uuid" to uuid, "state" to state.value))
    }

}