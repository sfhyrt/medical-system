package com.example.medical.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.medical.entity.*;
import com.example.medical.mapper.*;
import com.example.medical.dto.*;
import com.example.medical.exception.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DirectoryService {
 private final DepartmentMapper departments; private final DoctorMapper doctors; private final PatientMapper patients;
 public DirectoryService(DepartmentMapper departments,DoctorMapper doctors,PatientMapper patients){this.departments=departments;this.doctors=doctors;this.patients=patients;}
 private boolean text(String value){return value!=null&&!value.isBlank();}
 private <T>T required(T value){if(value==null)throw new ResourceNotFoundException();return value;}
 private <T>PageResult<T> page(Page<?> source,List<T> values){return new PageResult<>(values,source.getTotal(),source.getCurrent(),source.getSize(),source.getPages());}
 public PageResult<Department> departments(long page,long size,String code,String name,String location){
  var q=new LambdaQueryWrapper<Department>().like(text(code),Department::getCode,code).like(text(name),Department::getName,name).like(text(location),Department::getLocation,location).orderByAsc(Department::getId);
  var result=departments.selectPage(new Page<>(page,size),q);return page(result,result.getRecords());
 }
 public List<Department> departmentOptions(){return departments.selectList(new LambdaQueryWrapper<Department>().orderByAsc(Department::getName));}
 public Department department(long id){return required(departments.selectById(id));}
 public Department saveDepartment(Long id,DepartmentRequest request){
  Department value=id==null?new Department():department(id);value.setCode(request.code());value.setName(request.name());value.setLocation(request.location());value.setPhone(request.phone());value.setDescription(request.description()==null?"":request.description());
  if(id==null)departments.insert(value);else if(departments.updateById(value)==0)throw new ResourceNotFoundException();return value;
 }
 public void deleteDepartment(long id){if(departments.deleteById(id)==0)throw new ResourceNotFoundException();}
 public PageResult<Map<String,Object>> doctors(long page,long size,String code,String name,Long departmentId,String title,Integer status){
  var q=new LambdaQueryWrapper<Doctor>().like(text(code),Doctor::getCode,code).like(text(name),Doctor::getName,name).eq(departmentId!=null,Doctor::getDepartmentId,departmentId).like(text(title),Doctor::getTitle,title).eq(status!=null,Doctor::getStatus,status).orderByAsc(Doctor::getId);
  var result=doctors.selectPage(new Page<>(page,size),q);
  var ids=result.getRecords().stream().map(Doctor::getDepartmentId).collect(Collectors.toSet());
  Map<Long,String> names=ids.isEmpty()?Map.of():departments.selectByIds(ids).stream().collect(Collectors.toMap(Department::getId,Department::getName));
  List<Map<String,Object>> rows=result.getRecords().stream().map(d->{Map<String,Object> m=new LinkedHashMap<>();m.put("id",d.getId());m.put("code",d.getCode());m.put("name",d.getName());m.put("departmentId",d.getDepartmentId());m.put("departmentName",names.get(d.getDepartmentId()));m.put("title",d.getTitle());m.put("phone",d.getPhone());m.put("status",d.getStatus());return m;}).toList();
  return page(result,rows);
 }
 public Doctor doctor(long id){return required(doctors.selectById(id));}
 public Doctor saveDoctor(Long id,DoctorRequest request){
  Doctor value=id==null?new Doctor():doctor(id);if(departments.selectById(request.departmentId())==null)throw new InvalidReferenceException();
  value.setCode(request.code());value.setName(request.name());value.setDepartmentId(request.departmentId());value.setTitle(request.title());value.setPhone(request.phone());value.setStatus(request.status());
  if(id==null)doctors.insert(value);else if(doctors.updateById(value)==0)throw new ResourceNotFoundException();return value;
 }
 public void deleteDoctor(long id){if(doctors.deleteById(id)==0)throw new ResourceNotFoundException();}
 public PageResult<Map<String,Object>> patients(long page,long size,String name,String phone,String idCard){
  var q=new LambdaQueryWrapper<Patient>().like(text(name),Patient::getName,name).eq(text(phone),Patient::getPhone,phone).eq(text(idCard),Patient::getIdCard,idCard).orderByAsc(Patient::getId);
  var result=patients.selectPage(new Page<>(page,size),q);
  List<Map<String,Object>> rows=result.getRecords().stream().map(p->{Map<String,Object> m=new LinkedHashMap<>();m.put("id",p.getId());m.put("name",p.getName());m.put("phone",p.getPhone().substring(0,3)+"****"+p.getPhone().substring(7));m.put("idCard",p.getIdCard().substring(0,6)+"********"+p.getIdCard().substring(14));m.put("address",p.getAddress());return m;}).toList();
  return page(result,rows);
 }
 public Patient patient(long id){return required(patients.selectById(id));}
 public Patient savePatient(Long id,PatientRequest request){
  Patient value=id==null?new Patient():patient(id);value.setName(request.name());value.setIdCard(request.idCard());value.setPhone(request.phone());value.setAddress(request.address()==null?"":request.address());
  if(id==null)patients.insert(value);else if(patients.updateById(value)==0)throw new ResourceNotFoundException();return value;
 }
 public void deletePatient(long id){if(patients.deleteById(id)==0)throw new ResourceNotFoundException();}
}
