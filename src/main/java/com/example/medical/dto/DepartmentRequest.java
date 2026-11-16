package com.example.medical.dto;
import jakarta.validation.constraints.*;
public record DepartmentRequest(
 @NotBlank @Pattern(regexp="[A-Z0-9_-]{2,32}",message="科室编码需为2至32位大写字母、数字、下划线或短横线") String code,
 @NotBlank @Size(max=80) String name,
 @NotBlank @Size(max=120) String location,
 @NotBlank @Pattern(regexp="1[3-9]\\d{9}",message="手机号需为11位有效格式") String phone,
 @Size(max=500) String description) {}
