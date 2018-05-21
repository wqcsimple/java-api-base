package com.whis.app.service

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.InputStream
import javax.servlet.http.HttpServletResponse

@Service
class DownloadService {

    fun processImgDownload(response: HttpServletResponse, fileUrl: String) {
        val httpclient = HttpClients.createDefault()
        val httpPost = HttpGet(fileUrl)
        httpclient.execute(httpPost).use { res ->
            val entity = res.entity
            val inputStream = entity.content!!

            downloadImg(inputStream, response, "")
        }
    }

    fun downloadImg(inputStream: InputStream, response: HttpServletResponse, filename: String) {

//        response.contentType = "multipart/form-data"
        response.characterEncoding = "UTF-8"
        //2.设置文件头：最后一个参数是设置下载文件名(假如我们叫a.pdf)
        response.setHeader("Content-Disposition", "attachment;fileName=$filename")
//        response.addHeader("Content-Length", "" + file.length());

        try {

            //3.通过response获取ServletOutputStream对象(out)
            val out = response.outputStream

            val buffer = ByteArray(inputStream.available())
            var nRead = 0
            while (nRead != -1) {
                nRead = inputStream.read(buffer)
                //4.写到输出流(out)中
                out.write(buffer, 0, nRead)
            }
            out.close()
            out.flush()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}