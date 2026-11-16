package com.example.medical.service;

import com.example.medical.dto.PageResult;
import com.example.medical.exception.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class WorkflowQueryService {
    private final JdbcTemplate jdbc;
    public WorkflowQueryService(JdbcTemplate jdbc){this.jdbc=jdbc;}
    private List<Map<String,Object>> localDates(List<Map<String,Object>> rows){
        rows.forEach(row->row.replaceAll((key,value)->value instanceof java.sql.Date date?date.toLocalDate():value instanceof java.sql.Timestamp time?time.toLocalDateTime():value));
        return rows;
    }
    private PageResult<Map<String,Object>> page(String select,String from,String order,List<Object> args,long page,long size){
        long total=jdbc.queryForObject("SELECT COUNT(*) "+from,Long.class,args.toArray());
        var values=new ArrayList<>(args);values.add(size);values.add((page-1)*size);
        return new PageResult<>(localDates(jdbc.queryForList(select+from+order+" LIMIT ? OFFSET ?",values.toArray())),total,page,size,(total+size-1)/size);
    }
    public PageResult<Map<String,Object>> schedules(long page,long size,LocalDate date,Long doctorId){
        var args=new ArrayList<Object>();var from=new StringBuilder("FROM doctor_schedule s JOIN doctor d ON d.id=s.doctor_id JOIN department dep ON dep.id=d.department_id WHERE 1=1");
        if(date!=null){from.append(" AND s.visit_date=?");args.add(date);}if(doctorId!=null){from.append(" AND s.doctor_id=?");args.add(doctorId);}
        return page("SELECT s.id,s.doctor_id AS doctorId,d.name AS doctorName,dep.name AS departmentName,s.visit_date AS visitDate,s.session,s.capacity,s.remaining,s.enabled,d.status AS doctorStatus ",from.toString()," ORDER BY s.visit_date DESC,s.id DESC",args,page,size);
    }
    public PageResult<Map<String,Object>> registrations(long page,long size,String status,Long patientId){
        var args=new ArrayList<Object>();var from=new StringBuilder("FROM registration r JOIN patient p ON p.id=r.patient_id JOIN doctor d ON d.id=r.doctor_id WHERE 1=1");
        if(status!=null&&!status.isBlank()){from.append(" AND r.status=?");args.add(status);}if(patientId!=null){from.append(" AND r.patient_id=?");args.add(patientId);}
        return page("SELECT r.id,r.registration_no AS registrationNo,r.patient_id AS patientId,p.name AS patientName,d.name AS doctorName,r.schedule_id AS scheduleId,r.visit_time AS visitTime,r.status ",from.toString()," ORDER BY r.id DESC",args,page,size);
    }
    public PageResult<Map<String,Object>> prescriptions(long page,long size){
        return page("SELECT pr.id,pr.prescription_no AS prescriptionNo,pr.registration_id AS registrationId,p.name AS patientName,d.name AS doctorName,pr.diagnosis,pr.issued_at AS issuedAt ","FROM prescription pr JOIN registration r ON r.id=pr.registration_id JOIN patient p ON p.id=r.patient_id JOIN doctor d ON d.id=r.doctor_id"," ORDER BY pr.id DESC",List.of(),page,size);
    }
    public Map<String,Object> prescription(long id){
        var rows=localDates(jdbc.queryForList("SELECT id,prescription_no AS prescriptionNo,registration_id AS registrationId,diagnosis,issued_at AS issuedAt FROM prescription WHERE id=?",id));
        if(rows.isEmpty())throw new ResourceNotFoundException();var value=rows.getFirst();
        value.put("items",jdbc.queryForList("SELECT i.line_no AS lineNo,i.medicine_id AS medicineId,m.name AS medicineName,m.unit,i.dosage,i.frequency,i.days,i.quantity FROM prescription_item i JOIN medicine m ON m.id=i.medicine_id WHERE i.prescription_id=? ORDER BY i.line_no",id));return value;
    }
    public PageResult<Map<String,Object>> medicines(long page,long size){return page("SELECT id,code,name,unit,stock ","FROM medicine"," ORDER BY id",List.of(),page,size);}
}
