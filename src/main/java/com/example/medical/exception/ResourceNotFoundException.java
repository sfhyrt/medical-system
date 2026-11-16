package com.example.medical.exception;
public class ResourceNotFoundException extends RuntimeException {
 public ResourceNotFoundException(){super("记录不存在或已被删除");}
}
