package com.example.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.medical.entity.Medicine;
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
public interface MedicineMapper extends BaseMapper<Medicine> {
    @org.apache.ibatis.annotations.Update("UPDATE medicine SET stock=stock-#{quantity} WHERE id=#{id} AND stock>=#{quantity}")
    int consume(@org.apache.ibatis.annotations.Param("id") long id,@org.apache.ibatis.annotations.Param("quantity") java.math.BigDecimal quantity);


}
