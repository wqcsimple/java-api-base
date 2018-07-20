package com.whis.app.service

import com.whis.app.mapper.AdminMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TestService {

    companion object {
        private val logger = LoggerFactory.getLogger(TestService::class.java)
    }

    @Autowired lateinit var adminMapper: AdminMapper

    fun testDb() {
        val admin = adminMapper.finById(1L)

        logger.info("admin {}", admin)
    }


    fun testUtil() {
    }
}
