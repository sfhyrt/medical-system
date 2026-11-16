package com.example.medical.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;

@TableName("doctor_schedule")
public class DoctorSchedule {
    @TableId(type=IdType.AUTO) private Long id;
    private Long doctorId;
    private LocalDate visitDate;
    private String session;
    private Integer capacity;
    private Integer remaining;
    private Integer enabled;
    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public Long getDoctorId(){return doctorId;}
    public void setDoctorId(Long doctorId){this.doctorId=doctorId;}
    public LocalDate getVisitDate(){return visitDate;}
    public void setVisitDate(LocalDate visitDate){this.visitDate=visitDate;}
    public String getSession(){return session;}
    public void setSession(String session){this.session=session;}
    public Integer getCapacity(){return capacity;}
    public void setCapacity(Integer capacity){this.capacity=capacity;}
    public Integer getRemaining(){return remaining;}
    public void setRemaining(Integer remaining){this.remaining=remaining;}
    public Integer getEnabled(){return enabled;}
    public void setEnabled(Integer enabled){this.enabled=enabled;}
}
