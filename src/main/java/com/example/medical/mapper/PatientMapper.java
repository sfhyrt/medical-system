package com.example.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.medical.entity.Patient;
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
public interface PatientMapper extends BaseMapper<Patient> {

}
