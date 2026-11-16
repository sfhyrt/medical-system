package com.example.medical.dto;
import java.util.List;
public record PageResult<T>(List<T> records,long total,long page,long pageSize,long pages) {}
