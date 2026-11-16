package com.example.medical.validation;
import jakarta.validation.*;
import java.time.LocalDate;
import java.time.DateTimeException;
public class IdentityCardValidator implements ConstraintValidator<ValidIdentityCard,String> {
 private static final int[] WEIGHTS={7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2};
 public boolean isValid(String value,ConstraintValidatorContext context){return valid(value);}
 public static boolean valid(String value){
  if(value==null || !value.matches("[1-9]\\d{16}[0-9X]"))return false;
  try {
   LocalDate birth=LocalDate.of(Integer.parseInt(value.substring(6,10)),Integer.parseInt(value.substring(10,12)),Integer.parseInt(value.substring(12,14)));
   if(birth.isBefore(LocalDate.of(1900,1,1)) || birth.isAfter(LocalDate.now()) || value.substring(14,17).equals("000"))return false;
  }catch(DateTimeException|NumberFormatException exception){return false;}
  int sum=0;for(int i=0;i<17;i++)sum+=(value.charAt(i)-'0')*WEIGHTS[i];
  return value.charAt(17)=="10X98765432".charAt(sum%11);
 }
}
