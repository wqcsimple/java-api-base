package com.whis.app.controller;

import com.whis.app.service.HttpTestService;
import com.whis.app.service.TestService;
import com.whis.base.common.DataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("TestController")
@RequestMapping("/test")
@Controller
public class TestController {


    private final TestService testService;
    private final HttpTestService httpTestService;

    @Autowired
    public TestController(TestService testService, HttpTestService httpTestService) {
        this.testService = testService;
        this.httpTestService = httpTestService;
    }

    @RequestMapping("/test")
    public DataResponse test() {
        httpTestService.testGetRequest();
        return DataResponse.create().put("whis", "whis");
    }

    @RequestMapping("/md5")
    public DataResponse md5(@RequestParam("content") String content)
    {
        DataResponse dataResponse = new DataResponse();
        dataResponse.put("md5", DigestUtils.md5DigestAsHex(content.getBytes()));
        return dataResponse;
    }
}
