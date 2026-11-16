package com.example.medical.service;

import com.example.medical.dto.WorkflowRequests;
import com.example.medical.entity.DoctorSchedule;
import com.example.medical.exception.*;
import com.example.medical.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
public class ScheduleService {
    private final DoctorScheduleMapper schedules;
    private final DoctorMapper doctors;
    public ScheduleService(DoctorScheduleMapper schedules, DoctorMapper doctors){this.schedules=schedules;this.doctors=doctors;}

    @Transactional(rollbackFor=Exception.class)
    public DoctorSchedule create(WorkflowRequests.Schedule request){
        var doctor=doctors.selectById(request.doctorId());
        if(doctor==null)throw new ResourceNotFoundException();
        if(doctor.getStatus()!=1)throw new BusinessException("DOCTOR_UNAVAILABLE","医生已停诊，不能新增排班");
        if(request.visitDate().isBefore(LocalDate.now()))throw new BusinessException("SCHEDULE_EXPIRED","排班日期不能早于今天");
        var value=new DoctorSchedule();value.setDoctorId(request.doctorId());value.setVisitDate(request.visitDate());
        value.setSession(request.session());value.setCapacity(request.capacity());value.setRemaining(request.capacity());value.setEnabled(1);
        try{schedules.insert(value);}catch(org.springframework.dao.DuplicateKeyException e){throw new BusinessException("SCHEDULE_EXISTS","该医生在相同日期和时段已有排班");}
        return value;
    }

    @Transactional(rollbackFor=Exception.class)
    public DoctorSchedule update(long id, WorkflowRequests.ScheduleUpdate request){
        var value=schedules.lock(id);if(value==null)throw new ResourceNotFoundException();
        int used=value.getCapacity()-value.getRemaining();
        if(request.capacity()<used)throw new BusinessException("CAPACITY_TOO_SMALL","容量不能少于已占用的号源数（"+used+"）");
        value.setCapacity(request.capacity());value.setRemaining(request.capacity()-used);value.setEnabled(request.enabled());
        schedules.updateById(value);return value;
    }
}
