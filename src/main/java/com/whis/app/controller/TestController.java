package com.whis.app.controller;

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

    @Autowired
    public TestController(TestService testService) {
        this.testService = testService;
    }

    @RequestMapping("/test")
    public DataResponse test() {
        testService.testDb();
        return DataResponse.create();
    }

    @RequestMapping("/md5")
    public DataResponse md5(@RequestParam("content") String content)
    {
        DataResponse dataResponse = new DataResponse();
        dataResponse.put("md5", DigestUtils.md5DigestAsHex(content.getBytes()));
        return dataResponse;
    }
}
