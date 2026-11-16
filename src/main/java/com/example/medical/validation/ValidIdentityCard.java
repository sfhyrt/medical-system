package com.example.medical.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.RECORD_COMPONENT,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=IdentityCardValidator.class)
public @interface ValidIdentityCard {
 String message() default "身份证需为有效的18位号码，出生日期及校验码须正确";
 Class<?>[] groups() default {};
 Class<? extends Payload>[] payload() default {};
}
