package com.example.medical;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.medical.entity.Doctor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
class PreparedQueryTests {
 @Autowired SqlSessionFactory sessions;
 @Test void injectionPayloadIsBoundAsDataInsteadOfSql(){
  String attack="%' OR 1=1; DROP TABLE patient; --";
  var query=new QueryWrapper<Doctor>().like("name",attack);
  var statement=sessions.getConfiguration().getMappedStatement("com.example.medical.mapper.DoctorMapper.selectList");
  var sql=statement.getBoundSql(Map.of("ew",query));
  assertTrue(sql.getSql().contains("?"));
  assertFalse(sql.getSql().contains(attack));
  assertFalse(sql.getParameterMappings().isEmpty());
  assertTrue(query.getParamNameValuePairs().containsValue("%"+attack+"%"));
 }
}
