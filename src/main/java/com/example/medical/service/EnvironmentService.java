package com.example.medical.service;

import com.example.medical.dto.DatabaseSnapshot;
import com.example.medical.dto.EnvironmentReport;
import com.example.medical.mapper.DatabaseProbeMapper;
import java.time.OffsetDateTime;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentService {
    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentService.class);
    private final DatabaseProbeMapper mapper;
    private final HelloService helloService;

    public EnvironmentService(DatabaseProbeMapper mapper, HelloService helloService) {
        this.mapper = mapper;
        this.helloService = helloService;
    }

    public EnvironmentReport inspect() {
        try {
            DatabaseSnapshot snapshot = mapper.inspect();
            if (snapshot == null || snapshot.probe() != 1) {
                return report("unavailable", null, "数据库查询结果不符合预期，请检查本机配置。");
            }
            return report("connected", snapshot, "前端、后端与数据库连接正常");
        } catch (DataAccessException exception) {
            // Do not return connection strings, credentials or driver error details.
            LOG.warn("Database probe failed: {}", exception.getMostSpecificCause().getClass().getSimpleName());
            return report("unavailable", null, "数据库连接失败，请检查本机登录配置和 MySQL 服务。");
        }
    }

    private EnvironmentReport report(String status, DatabaseSnapshot snapshot, String message) {
        return new EnvironmentReport("医疗系统 · 实验一", helloService.hello(),
                System.getProperty("java.version"), SpringBootVersion.getVersion(),
                SqlSessionFactory.class.getPackage().getImplementationVersion(), status,
                snapshot == null ? null : snapshot.databaseVersion(),
                snapshot == null ? null : snapshot.serverTime(),
                snapshot == null ? null : snapshot.probe(), OffsetDateTime.now().toString(), message);
    }
}
