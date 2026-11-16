package com.example.medical.dto;
import jakarta.validation.constraints.*;
public record DoctorRequest(
 @NotBlank @Pattern(regexp="[A-Z0-9_-]{2,32}",message="医生编码格式不正确") String code,
 @NotBlank @Size(max=80) String name,
 @NotNull @Positive Long departmentId,
 @NotBlank @Size(max=50) String title,
 @NotBlank @Pattern(regexp="1[3-9]\\d{9}",message="手机号需为11位有效格式") String phone,
 @NotNull @Min(0) @Max(1) Integer status) {}
