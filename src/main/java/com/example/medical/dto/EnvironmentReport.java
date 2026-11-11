package com.example.medical.dto;

public record EnvironmentReport(
        String application, String hello, String javaVersion, String springBootVersion,
        String mybatisVersion, String databaseStatus, String databaseVersion,
        String databaseTime, Integer probe, String checkedAt, String message) {
}
