package com.whis

import com.whis.app.Application
import com.whis.app.service.HttpTestService
import com.whis.base.common.RequestUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension


@SpringBootTest(classes = [(Application::class)])
@ExtendWith(SpringExtension::class)
class TestServiceTest {

    private val logger = LoggerFactory.getLogger(TestServiceTest::class.java)


    @Autowired lateinit var httpTestService: HttpTestService

    @Test
    fun test() {
        httpTestService.testGetRequest()
//        httpTestService.testPostRequest()

    }

}