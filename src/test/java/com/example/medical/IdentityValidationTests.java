package com.example.medical;
import com.example.medical.validation.IdentityCardValidator;
import com.example.medical.dto.PatientRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class IdentityValidationTests {
 private String id(String body){int[] w={7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2};int sum=0;for(int i=0;i<17;i++)sum+=(body.charAt(i)-'0')*w[i];return body+"10X98765432".charAt(sum%11);}
 @Test void acceptsValidDateAndChecksum(){assertTrue(IdentityCardValidator.valid(id("11010119900101001")));}
 @Test void rejectsWrongChecksum(){String valid=id("11010119900101001");assertFalse(IdentityCardValidator.valid(valid.substring(0,17)+(valid.endsWith("1")?"2":"1")));}
 @Test void rejectsImpossibleCalendarDate(){assertFalse(IdentityCardValidator.valid(id("11010120010230001")));}
 @Test void rejectsFutureBirth(){assertFalse(IdentityCardValidator.valid(id("11010120990101001")));}
 @Test void rejectsWrongLengthAndBlank(){for(String value:new String[]{"","123", "1101011990010100A1"})assertFalse(IdentityCardValidator.valid(value));assertFalse(IdentityCardValidator.valid(null));}
 @Test void rejectsZeroSequence(){assertFalse(IdentityCardValidator.valid(id("11010119900101000")));}
 @Test void validatesMobileFormatAndRequiredName(){
  try(var factory=Validation.buildDefaultValidatorFactory()){
   var validator=factory.getValidator();String identity=id("11010119900101001");
   assertTrue(validator.validate(new PatientRequest("Synthetic patient",identity,"13900001001","")).isEmpty());
   for(String phone:new String[]{"12345","10000001001","1390000100A","１３９００００１００１"})assertFalse(validator.validate(new PatientRequest("Synthetic patient",identity,phone,"")).isEmpty());
   assertFalse(validator.validate(new PatientRequest("   ",identity,"13900001001","")).isEmpty());
  }
 }
}
