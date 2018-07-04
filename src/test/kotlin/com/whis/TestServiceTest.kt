package com.whis

import com.whis.app.Application
import com.whis.base.common.Util
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension


@SpringBootTest(classes = [(Application::class)])
@ExtendWith(SpringExtension::class)
class TestServiceTest {

    private val logger = LoggerFactory.getLogger(TestServiceTest::class.java)


    @Test
    fun test() {

    }

    @Test
    fun utilSafeGet() {
        val map = hashMapOf<String, Any>(
                "name" to "whis",
                "age" to 25
        )

        logger.info("name {}", Util.safeGet(map, "name", String::class.java, ""))
        logger.info("age {}", Util.safeGet(map, "age", Long::class.java, 0))
        logger.info("gender {}", Util.safeGet(map, "gender", Int::class.java, 0))
    }
}