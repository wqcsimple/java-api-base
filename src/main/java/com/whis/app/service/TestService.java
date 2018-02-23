package com.whis.app.service;

import com.whis.app.mapper.AdminMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TestService {

    private static Logger logger = LoggerFactory.getLogger(TestService.class);

    private final AdminMapper adminMapper;

    @Autowired
    public TestService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public void testDb() {
        Map<String, Object> admin = adminMapper.finById(1L);

        logger.info("admin {}", admin);
    }
}
