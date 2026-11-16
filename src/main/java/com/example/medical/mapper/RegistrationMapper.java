package com.example.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.medical.entity.Registration;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Medical System
 * @since 2026-11-16
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
    @org.apache.ibatis.annotations.Select("SELECT id FROM registration WHERE patient_id=#{patientId} AND schedule_id=#{scheduleId} AND status<>'CANCELLED' LIMIT 1 FOR UPDATE")
    Long active(@org.apache.ibatis.annotations.Param("patientId") long patientId, @org.apache.ibatis.annotations.Param("scheduleId") long scheduleId);
    @org.apache.ibatis.annotations.Select("SELECT * FROM registration WHERE id=#{id} FOR UPDATE")
    Registration lock(@org.apache.ibatis.annotations.Param("id") long id);


}
