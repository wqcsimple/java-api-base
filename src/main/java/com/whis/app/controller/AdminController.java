package com.whis.app.controller;

import com.whis.app.service.AdminService;
import com.whis.base.common.DataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("AdminController")
@RequestMapping("/admin")
@Controller
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @RequestMapping("/info")
    public DataResponse adminInfo(@RequestParam("id") Long id) {
        Map<String, Object> data = adminService.getAdminInfo(id);
        return DataResponse.create().put("admin", data);
    }

}
