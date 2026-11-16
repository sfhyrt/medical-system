package com.example.medical.codegen;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
public class PersistenceGenerator {
 public static void main(String[] args) throws Exception {
  try { run(args); } finally { com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown(); }
 }
 private static void run(String[] args) throws Exception {
  Path root=Path.of(System.getProperty("user.dir")).toAbsolutePath();
  while(!Files.isDirectory(root.resolve("frontend"))) { root=root.getParent(); if(root==null)throw new IllegalStateException("Run from the project root"); }
  Properties p=new Properties();
  if(Files.exists(root.resolve("application-local.properties")))try(var in=Files.newInputStream(root.resolve("application-local.properties"))){p.load(in);}
  String user=p.getProperty("spring.datasource.username",System.getenv().getOrDefault("DB_USERNAME","root"));
  String pass=p.getProperty("spring.datasource.password",System.getenv().getOrDefault("DB_PASSWORD",""));
  String base="jdbc:mysql://127.0.0.1:3306/";
  String options="?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
  String url=p.getProperty("spring.datasource.url",System.getenv().getOrDefault("DB_URL",base+"medical_system"+options));
  if(args.length>0 && args[0].equals("--initialize")) {
   try(Connection c=DriverManager.getConnection(base+options,user,pass);Statement s=c.createStatement()) {
    String sql=Files.readString(root.resolve("sql/schema.sql")).replaceAll("(?m)^--.*$","");
    for(String part:sql.split(";"))if(!part.isBlank())s.execute(part);
    System.out.println("Database schema initialized without replacing existing rows.");
   }
   return;
  }
  Path output=args.length>0 && args[0].equals("--verify-generation") ? root.resolve("target/generated-persistence") : root;
  FastAutoGenerator.create(url,user,pass)
    .globalConfig(b->b.author("Medical System").disableOpenDir().outputDir(output.resolve("src/main/java").toString()))
    .packageConfig(b->b.parent("com.example.medical").pathInfo(Map.of(OutputFile.xml,output.resolve("src/main/resources/mapper").toString())))
    .strategyConfig(b->{b.addInclude("department","doctor","patient","registration","prescription","medicine","prescription_item");
       b.entityBuilder().idType(IdType.AUTO).enableTableFieldAnnotation();
       b.mapperBuilder().enableMapperAnnotation().enableBaseResultMap().enableBaseColumnList();
       b.serviceBuilder().disable();b.controllerBuilder().disable();})
    .templateEngine(new FreemarkerTemplateEngine()).execute();
  System.out.println("Generated entities, mappers and XML mappings from seven tables. Existing source files are preserved.");
 }
}
