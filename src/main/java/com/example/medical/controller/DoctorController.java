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

@RestController @Validated @RequestMapping("/api/doctors")
public class DoctorController {
 private final DirectoryService service;
 public DoctorController(DirectoryService service){this.service=service;}

 @GetMapping public PageResult<Map<String,Object>> list(@RequestParam(defaultValue="1") @Min(1) long page,@RequestParam(defaultValue="10") @Min(1) @Max(100) long pageSize,@RequestParam(required=false) String code,@RequestParam(required=false) String name,@RequestParam(required=false) @Positive Long departmentId,@RequestParam(required=false) String title,@RequestParam(required=false) @Min(0) @Max(1) Integer status){return service.doctors(page,pageSize,code,name,departmentId,title,status);}
 @GetMapping("/{id}") public Doctor get(@PathVariable @Positive long id){return service.doctor(id);}
 @PostMapping public ResponseEntity<Doctor> create(@Valid @RequestBody DoctorRequest request){var value=service.saveDoctor(null,request);return ResponseEntity.status(HttpStatus.CREATED).body(value);}
 @PutMapping("/{id}") public Doctor update(@PathVariable @Positive long id,@Valid @RequestBody DoctorRequest request){return service.saveDoctor(id,request);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable @Positive long id){service.deleteDoctor(id);return ResponseEntity.noContent().build();}
}
