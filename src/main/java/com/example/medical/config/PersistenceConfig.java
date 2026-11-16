package com.example.medical.config;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.*;
@Configuration
public class PersistenceConfig {
 @Bean public MybatisPlusInterceptor mybatisPlusInterceptor(){
  var interceptor=new MybatisPlusInterceptor();
  var pagination=new PaginationInnerInterceptor(DbType.MYSQL);
  pagination.setMaxLimit(100L); pagination.setOverflow(false);
  interceptor.addInnerInterceptor(pagination); return interceptor;
 }
}
