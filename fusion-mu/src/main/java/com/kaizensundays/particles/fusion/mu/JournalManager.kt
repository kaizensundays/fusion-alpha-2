package com.kaizensundays.particles.fusion.mu

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.kaizensundays.particles.fusion.mu.dao.JournalDao
import com.kaizensundays.particles.fusion.mu.messages.Event
import com.kaizensundays.particles.fusion.mu.messages.JacksonObjectConverter
import com.kaizensundays.particles.fusion.mu.messages.Journal
import com.kaizensundays.particles.fusion.mu.messages.JournalFormatted
import com.kaizensundays.particles.fusion.mu.messages.JournalState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*
import java.util.concurrent.BlockingQueue

/**
 * Created: Sunday 3/12/2023, 1:33 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
class JournalManager(private val journalDao: JournalDao) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val jsonConverter = JacksonObjectConverter<Event>()
    private val yamlConverter = JacksonObjectConverter<Event>(YAMLFactory())

    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z")

    lateinit var messageQueue: BlockingQueue<Journal>

    fun accept(event: Event) {

        val msg = jsonConverter.fromObject(event)
        logger.info(msg)

        val journal = Journal(0, JournalState.ACCEPTED.value, Date(), UUID.randomUUID().toString(), msg, event)

        journalDao.insert(journal)

        messageQueue.put(journal)
    }

    fun load() {

        journalDao.createTableIfItDoesNotExist()

        val journals = journalDao.findByState(JournalState.ACCEPTED.value)

        if (journals.isNotEmpty()) {
            logger.info("Recovering ${journals.size} accepted events")
        }

        journals.forEach { journal ->
            journal.event = jsonConverter.toObject(journal.msg)
            messageQueue.put(journal)
            logger.info(journal.toString())
        }

        logger.info("Loaded ${journals.size} accepted events")

        val ttl = Duration.ofHours(1)

        val time = Date(System.currentTimeMillis() - ttl.toMillis())

        val records = journalDao.findBefore(time, JournalState.COMMITTED)

        logger.info("Found ${records.size} committed events older that $ttl")
    }

    fun commit(journal: Journal): Int {
        return journalDao.updateStateByUUID(journal.uuid, JournalState.COMMITTED)
    }

    fun findAll(): List<JournalFormatted> {

        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzzz")
        df.timeZone = TimeZone.getTimeZone("America/New_York")

        val journals = journalDao.findAll()

        return journals.map { journal ->
            val event = jsonConverter.toObject(journal.msg)
            val time = df.format(journal.time)
            JournalFormatted(journal.id, journal.state, time, journal.uuid, journal.msg, event)
        }
    }

}