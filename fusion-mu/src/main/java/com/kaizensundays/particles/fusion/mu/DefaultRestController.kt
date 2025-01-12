package com.kaizensundays.particles.fusion.mu

import com.kaizensundays.particles.fusion.mu.messages.AddAirline
import com.kaizensundays.particles.fusion.mu.messages.JournalFormatted
import org.apache.ignite.IgniteCache
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * Created: Saturday 12/4/2021, 12:29 PM Eastern Time
 *
 * @author Sergey Chuykov
 */
@RestController
class DefaultRestController(
    private val resultsCache: IgniteCache<String, String>,
    private val eventRoute: DefaultEventRoute,
    private val journalManager: JournalManager
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @ResponseBody
    @RequestMapping("/ping")
    fun ping(): String {
        return "Ok"
    }

    @ResponseBody
    @PostMapping("/add/airline")
    fun addAirline(@RequestBody addAirline: AddAirline): String {

        eventRoute.handle(addAirline)

        return "Ok"
    }

    @GetMapping("/get/result/{key}")
    fun getResult(@PathVariable("key") key: String): String {
        logger.info("key={}", key)
        try {
            return resultsCache[key]
        } catch (e: Exception) {
            logger.error("", e)
        }
        return "?"
    }

    @GetMapping("/put/result/{key}/{value}")
    fun getResult(@PathVariable("key") key: String, @PathVariable("value") value: String): String {
        logger.info("key={} value={}", key, value)
        try {
            resultsCache.put(key, value)
        } catch (e: Exception) {
            logger.error("No Quorum: " + e.message)
        }
        return "Ok"
    }

    @GetMapping("/journal/findAll")
    fun journalFindAll(): List<JournalFormatted> {

        return journalManager.findAll()
    }

}