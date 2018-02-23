package com.whis.app.service;

import com.whis.app.mapper.AdminMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AdminService {

    private static Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final AdminMapper adminMapper;

    @Autowired
    public AdminService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public Map<String, Object> getAdminInfo(Long id) {
        return adminMapper.finById(id);
    }
}
