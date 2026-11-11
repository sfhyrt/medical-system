package com.example.medical.dto;

/** Values returned by a real, read-only MySQL query. */
public record DatabaseSnapshot(String databaseVersion, String serverTime, int probe) {
}
