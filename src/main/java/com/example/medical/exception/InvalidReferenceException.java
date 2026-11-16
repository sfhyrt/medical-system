package com.example.medical.exception;
public class InvalidReferenceException extends RuntimeException {
 public InvalidReferenceException(){super("所选科室不存在，请重新选择");}
}
