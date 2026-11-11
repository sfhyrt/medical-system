package com.example.medical.mapper;

import com.example.medical.dto.DatabaseSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DatabaseProbeMapper {

    // No application tables are read or changed; no default schema is required.
    @Select("SELECT VERSION() AS databaseVersion, CAST(CURRENT_TIMESTAMP AS CHAR) AS serverTime, 1 AS probe")
    DatabaseSnapshot inspect();
}
