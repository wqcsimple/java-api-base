package com.whis.app.controller

import com.whis.app.service.DownloadService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

@RestController("StaticController")
@RequestMapping("/static")
@Controller
class StaticController {

    @Autowired
    lateinit var downloadService: DownloadService

    @RequestMapping("/file")
    fun imgDownload(@RequestParam("url") url: String, response: HttpServletResponse) {
        downloadService.processImgDownload(response, url)
    }


}
