package com.example.medical.controller;

import com.example.medical.dto.*;
import com.example.medical.entity.*;
import com.example.medical.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.time.LocalDate;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api")
public class WorkflowController {
    private final ScheduleService schedules;
    private final RegistrationService registrations;
    private final PrescriptionService prescriptions;
    private final WorkflowQueryService queries;
    public WorkflowController(ScheduleService schedules,RegistrationService registrations,PrescriptionService prescriptions,WorkflowQueryService queries){this.schedules=schedules;this.registrations=registrations;this.prescriptions=prescriptions;this.queries=queries;}
    @GetMapping("/schedules") public PageResult<Map<String,Object>> schedules(@RequestParam(defaultValue="1") @Min(1) @Max(1000000) long page,@RequestParam(defaultValue="10") @Min(1) @Max(100) long pageSize,@RequestParam(required=false) LocalDate visitDate,@RequestParam(required=false) @Positive Long doctorId){return queries.schedules(page,pageSize,visitDate,doctorId);}
    @PostMapping("/schedules") public ResponseEntity<DoctorSchedule> createSchedule(@Valid @RequestBody WorkflowRequests.Schedule request){return ResponseEntity.status(HttpStatus.CREATED).body(schedules.create(request));}
    @PutMapping("/schedules/{id}") public DoctorSchedule updateSchedule(@PathVariable @Positive long id,@Valid @RequestBody WorkflowRequests.ScheduleUpdate request){return schedules.update(id,request);}
    @GetMapping("/registrations") public PageResult<Map<String,Object>> registrations(@RequestParam(defaultValue="1") @Min(1) @Max(1000000) long page,@RequestParam(defaultValue="10") @Min(1) @Max(100) long pageSize,@RequestParam(required=false) @Pattern(regexp="BOOKED|CANCELLED|COMPLETED") String status,@RequestParam(required=false) @Positive Long patientId){return queries.registrations(page,pageSize,status,patientId);}
    @PostMapping("/registrations") public ResponseEntity<Registration> book(@Valid @RequestBody WorkflowRequests.Booking request){return ResponseEntity.status(HttpStatus.CREATED).body(registrations.book(request));}
    @PostMapping("/registrations/{id}/cancel") public Registration cancel(@PathVariable @Positive long id){return registrations.cancel(id);}
    @GetMapping("/prescriptions") public PageResult<Map<String,Object>> prescriptions(@RequestParam(defaultValue="1") @Min(1) @Max(1000000) long page,@RequestParam(defaultValue="10") @Min(1) @Max(100) long pageSize){return queries.prescriptions(page,pageSize);}
    @GetMapping("/prescriptions/{id}") public Map<String,Object> prescription(@PathVariable @Positive long id){return queries.prescription(id);}
    @PostMapping("/prescriptions") public ResponseEntity<Prescription> issue(@Valid @RequestBody WorkflowRequests.Issue request){return ResponseEntity.status(HttpStatus.CREATED).body(prescriptions.issue(request));}
    @GetMapping("/medicines") public PageResult<Map<String,Object>> medicines(@RequestParam(defaultValue="1") @Min(1) @Max(1000000) long page,@RequestParam(defaultValue="10") @Min(1) @Max(100) long pageSize){return queries.medicines(page,pageSize);}
}
