package com.example.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.medical.entity.DoctorSchedule;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {
    @Select("SELECT * FROM doctor_schedule WHERE id=#{id} FOR UPDATE")
    DoctorSchedule lock(@Param("id") long id);
    @Update("UPDATE doctor_schedule SET remaining=remaining-1 WHERE id=#{id} AND remaining>0 AND enabled=1")
    int reserve(@Param("id") long id);
    @Update("UPDATE doctor_schedule SET remaining=remaining+1 WHERE id=#{id} AND remaining<capacity")
    int release(@Param("id") long id);
}
