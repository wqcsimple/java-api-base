package com.whis.app.service

import com.whis.base.common.RequestUtil
import com.whis.base.exception.NotAllowedException
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json



@Service
class HttpTestService {
    companion object {
        private val logger = LoggerFactory.getLogger(HttpTestService::class.java)
        val okHttpClient: OkHttpClient = OkHttpClient()

        val JSON = MediaType.parse("application/json; charset=utf-8")
        val URL_ENCODE = MediaType.parse("application/x-www-form-urlencoded")
    }


    fun testGetRequest() {
        val url = "http://api.crm.lishu.sinfere.com/client/1/queue/queue-info"

        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->

            logger.info("response {}", response)
            if (!response.isSuccessful) {
                throw NotAllowedException("调用失败")
            }

            val result = response.body()!!.string()
            logger.info("result {}", result)
        }
    }

    fun testPostRequest() {
        val url = "http://api.crm.lishu.sinfere.com/client/1/queue/queue-info"


        val requestParams = hashMapOf<String, Any>(
                "token" to "3bedbe0774d453b2ce55c29ef421efcf",
                "sp" to 4
        )
        val requestBody = RequestBody.create(URL_ENCODE, RequestUtil.parseParams(requestParams))
        val request = Request.Builder().url(url).post(requestBody).build()
        okHttpClient.newCall(request).execute().use { response ->

            logger.info("response {}", response)
            if (!response.isSuccessful) {
                throw NotAllowedException("调用失败")
            }

            val result = response.body()!!.string()
            logger.info("result {}", result)
        }
    }
}