package com.whis.app.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Service
class ThreadService {

    private val logger = LoggerFactory.getLogger(ThreadService::class.java)

    // 新起8个线程
    private val asyncExecutor: ThreadPoolExecutor = Executors.newFixedThreadPool(8) as ThreadPoolExecutor

    private fun updateOperations() {
        asyncExecutor.run {
            // todo
        }
    }
}