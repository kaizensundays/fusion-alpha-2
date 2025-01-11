package com.kaizensundays.particles.fusion.mu.dao

import com.kaizensundays.particles.fusion.mu.JournalContext
import com.kaizensundays.particles.fusion.mu.messages.Journal
import com.kaizensundays.particles.fusion.mu.messages.JournalState
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals

/**
 * Created: Saturday 3/11/2023, 11:13 AM Eastern Time
 *
 * @author Sergey Chuykov
 */
@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [JournalContext::class])
class JournalDaoTest {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var dao: JournalDao

    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z")

    private fun journal(msg: String): Journal {
        val time = df.parse("2023-03-19 11:00:00.000 EST")
        return Journal(
            0, JournalState.ACCEPTED.value, time,
            UUID.randomUUID().toString(), msg
        )
    }

    private val journals = listOf(
        journal("{1}"),
        journal("{2}"),
        journal("{3}"),
    )

    @Before
    fun before() {
        df.timeZone = TimeZone.getTimeZone("America/New_York")

        val existed = dao.createTableIfItDoesNotExist()
        if (existed) {
            dao.truncate()
        }
    }

    @After
    fun after() {
        dao.truncate()
    }

    @Test
    fun insert() {

        journals.forEach { journal -> dao.insert(journal) }

        val list = dao.findAll()

        assertEquals(journals.size, list.size)
    }

    @Test
    fun findBefore() {

        journals.forEach { journal -> dao.insert(journal) }

        val expectedSize = arrayOf(0, 0, 3)

        val timestamps = arrayOf(
            "2023-03-19 10:59:59.000 EST",
            "2023-03-19 11:00:00.000 EST", // records time
            "2023-03-19 11:00:01.000 EST",
        )

        timestamps.forEachIndexed { idx, ts ->

            val time = df.parse(ts)

            val records = dao.findBefore(time, JournalState.ACCEPTED)

            assertEquals(expectedSize[idx], records.size)
        }

    }

}