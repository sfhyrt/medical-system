package com.example.medical.controller;
import com.example.medical.dto.*;
import com.example.medical.entity.*;
import com.example.medical.service.DirectoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @Validated @RequestMapping("/api/patients")
public class PatientController {
 private final DirectoryService service;
 public PatientController(DirectoryService service){this.service=service;}

 @GetMapping public PageResult<Map<String,Object>> list(@RequestParam(defaultValue="1") @Min(1) long page,@RequestParam(defaultValue="10") @Min(1) @Max(100) long pageSize,@RequestParam(required=false) String name,@RequestParam(required=false) String phone,@RequestParam(required=false) String idCard){return service.patients(page,pageSize,name,phone,idCard);}
 @GetMapping("/{id}") public Patient get(@PathVariable @Positive long id){return service.patient(id);}
 @PostMapping public ResponseEntity<Patient> create(@Valid @RequestBody PatientRequest request){var value=service.savePatient(null,request);return ResponseEntity.status(HttpStatus.CREATED).body(value);}
 @PutMapping("/{id}") public Patient update(@PathVariable @Positive long id,@Valid @RequestBody PatientRequest request){return service.savePatient(id,request);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable @Positive long id){service.deletePatient(id);return ResponseEntity.noContent().build();}
}
