package com.example.medical.dto;
import com.example.medical.validation.ValidIdentityCard;
import jakarta.validation.constraints.*;
public record PatientRequest(
 @NotBlank @Size(max=80) String name,
 @NotBlank @ValidIdentityCard String idCard,
 @NotBlank @Pattern(regexp="1[3-9]\\d{9}",message="手机号需为11位有效格式") String phone,
 @Size(max=200) String address) {}
