package com.whis.app.service

import com.fasterxml.jackson.core.type.TypeReference
import com.whis.base.common.Util
import com.whis.base.exception.WrongParamException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ToolService {

    companion object {
        private val logger = LoggerFactory.getLogger(ToolService::class.java)

        private val validMimeTypes = "image/jpeg;image/png;application/pdf;application/msword;application/vnd.ms-excel;application/vnd.ms-powerpoint;application/vnd.openxmlformats-officedocument.wordprocessingml.document;"

    }


    /**
     * 处理前端传来的json传是否包含指定字段，和内容的过滤
     */
    fun parseAttachment(attachment: String): String {
        val attachmentList = mutableListOf<Map<String, Any>>()
        val list = Util.jsonDecode(attachment, object: TypeReference<List<Map<String, Any>>>(){})
        if (list != null && list.isNotEmpty()) {
            list.forEach { item ->
                val name = Util.safeGet(item, "name", String::class.java, "")
                val mimeType = Util.safeGet(item, "mime_type", String::class.java, "")
                if (validMimeTypes.indexOf(mimeType) < 0) {
                    throw WrongParamException("invalid mime_type $mimeType")
                }
                val file = Util.safeGet(item, "hash", String::class.java, "")!!
                val size = Util.safeGet(item, "size", Int::class.java, 0)!!
                val attach = mutableMapOf<String, Any>()
                attach["name"] = name
                attach["mime_type"] = mimeType
                attach["hash"] = file
                attach["size"] = size
                attachmentList.add(attach)
            }
        }

        if (attachmentList.isEmpty()) {
            return ""
        }
        return Util.jsonEncode(attachmentList)
    }

}
