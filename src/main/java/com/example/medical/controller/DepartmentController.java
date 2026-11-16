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

@RestController @Validated @RequestMapping("/api/departments")
public class DepartmentController {
 private final DirectoryService service;
 public DepartmentController(DirectoryService service){this.service=service;}
 @GetMapping("/options") public List<Department> options(){return service.departmentOptions();}
 @GetMapping public PageResult<Department> list(@RequestParam(defaultValue="1") @Min(1) long page,@RequestParam(defaultValue="10") @Min(1) @Max(100) long pageSize,@RequestParam(required=false) String code,@RequestParam(required=false) String name,@RequestParam(required=false) String location){return service.departments(page,pageSize,code,name,location);}
 @GetMapping("/{id}") public Department get(@PathVariable @Positive long id){return service.department(id);}
 @PostMapping public ResponseEntity<Department> create(@Valid @RequestBody DepartmentRequest request){var value=service.saveDepartment(null,request);return ResponseEntity.status(HttpStatus.CREATED).body(value);}
 @PutMapping("/{id}") public Department update(@PathVariable @Positive long id,@Valid @RequestBody DepartmentRequest request){return service.saveDepartment(id,request);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable @Positive long id){service.deleteDepartment(id);return ResponseEntity.noContent().build();}
}
