package com.example.medical.service;

import com.example.medical.dto.WorkflowRequests;
import com.example.medical.entity.Registration;
import com.example.medical.exception.*;
import com.example.medical.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.UUID;

@Service
public class RegistrationService {
    private final DoctorScheduleMapper schedules;
    private final RegistrationMapper registrations;
    private final PatientMapper patients;
    private final DoctorMapper doctors;
    public RegistrationService(DoctorScheduleMapper schedules, RegistrationMapper registrations, PatientMapper patients, DoctorMapper doctors){
        this.schedules=schedules;this.registrations=registrations;this.patients=patients;this.doctors=doctors;
    }

    @Transactional(rollbackFor=Exception.class)
    public Registration book(WorkflowRequests.Booking request){
        var schedule=schedules.lock(request.scheduleId());
        if(schedule==null||patients.selectById(request.patientId())==null)throw new ResourceNotFoundException();
        if(schedule.getEnabled()!=1)throw new BusinessException("SCHEDULE_DISABLED","该排班已暂停挂号");
        if(schedule.getVisitDate().isBefore(LocalDate.now()))throw new BusinessException("SCHEDULE_EXPIRED","该排班已过期，请选择其他日期");
        var doctor=doctors.selectById(schedule.getDoctorId());
        if(doctor==null||doctor.getStatus()!=1)throw new BusinessException("DOCTOR_UNAVAILABLE","医生已停诊，请选择其他排班");
        if(registrations.active(request.patientId(),schedule.getId())!=null)throw new BusinessException("DUPLICATE_BOOKING","该患者已预约此排班，请勿重复挂号");
        if(schedules.reserve(schedule.getId())!=1)throw new BusinessException("SCHEDULE_FULL","该排班号源已满，请选择其他时段");
        var value=new Registration();value.setRegistrationNo("REG"+UUID.randomUUID().toString().replace("-",""));
        value.setPatientId(request.patientId());value.setDoctorId(schedule.getDoctorId());value.setScheduleId(schedule.getId());
        value.setVisitTime(schedule.getVisitDate().atTime("AM".equals(schedule.getSession())?9:14,0));value.setStatus("BOOKED");
        registrations.insert(value);return value;
    }

    @Transactional(rollbackFor=Exception.class)
    public Registration cancel(long id){
        var existing=registrations.selectById(id);if(existing==null)throw new ResourceNotFoundException();
        // All cancellation paths acquire the schedule before the registration row.
        if(existing.getScheduleId()!=null)schedules.lock(existing.getScheduleId());
        var value=registrations.lock(id);
        if("CANCELLED".equals(value.getStatus()))return value;
        if(!"BOOKED".equals(value.getStatus()))throw new BusinessException("REGISTRATION_COMPLETED","已完成就诊的挂号不能取消");
        value.setStatus("CANCELLED");registrations.updateById(value);
        if(value.getScheduleId()!=null && schedules.release(value.getScheduleId())!=1)throw new IllegalStateException("Schedule quota is inconsistent");
        return value;
    }
}
