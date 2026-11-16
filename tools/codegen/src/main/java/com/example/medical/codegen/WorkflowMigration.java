package com.example.medical.codegen;
import java.nio.file.*;import java.sql.*;import java.util.*;import java.util.regex.*;
public class WorkflowMigration {
 public static void main(String[] args)throws Exception {
  Path root=Path.of(System.getProperty("user.dir"));Properties p=new Properties();
  if(Files.exists(root.resolve("application-local.properties")))try(var in=Files.newInputStream(root.resolve("application-local.properties"))){p.load(in);}
  String url=p.getProperty("spring.datasource.url",System.getenv().getOrDefault("DB_URL","jdbc:mysql://127.0.0.1:3306/medical_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"));
  try(Connection c=DriverManager.getConnection(url,p.getProperty("spring.datasource.username",System.getenv().getOrDefault("DB_USERNAME","root")),p.getProperty("spring.datasource.password",System.getenv().getOrDefault("DB_PASSWORD","")));Statement s=c.createStatement()){
   if(args.length>0 && args[0].equals("--sample")){
    String sql=Files.readString(root.resolve("sql/workflow-sample-data.sql")).replaceAll("(?m)^--.*$","");
    for(String part:sql.split(";"))if(!part.isBlank())s.execute(part);
    System.out.println("Synthetic inventory and schedules loaded without resetting existing counters.");return;
   }
   if(args.length>0 && args[0].equals("--backup")) {
    StringBuilder out=new StringBuilder("{\n");boolean first=true;
    for(String table:List.of("department","doctor","patient","registration","prescription","medicine","prescription_item")){
     if(!first)out.append(",\n");first=false;out.append(quote(table)).append(": [");
     try(ResultSet r=s.executeQuery("SELECT * FROM "+table)){boolean row=true;while(r.next()){if(!row)out.append(',');row=false;out.append('{');for(int i=1;i<=r.getMetaData().getColumnCount();i++){if(i>1)out.append(',');out.append(quote(r.getMetaData().getColumnName(i))).append(':').append(r.getObject(i)==null?"null":quote(r.getString(i)));}out.append('}');}}
     out.append(']');
    }
    Files.writeString(Path.of(args[1]),out.append("\n}\n").toString());System.out.println("Existing project rows backed up.");return;
   }
   String sql=Files.readString(root.resolve("sql/migrations/002_booking_inventory.sql")).replaceAll("(?m)^--.*$","");int applied=0,skipped=0;
   for(String part:sql.split(";")){
    part=part.trim();if(part.isEmpty())continue;
    Matcher col=Pattern.compile("ALTER TABLE (\\w+) ADD COLUMN (\\w+)",Pattern.CASE_INSENSITIVE).matcher(part);
    Matcher key=Pattern.compile("ALTER TABLE (\\w+) ADD CONSTRAINT (\\w+)",Pattern.CASE_INSENSITIVE).matcher(part);
    boolean exists=false;
    if(col.find()){try(var q=c.prepareStatement("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name=? AND column_name=?")){q.setString(1,col.group(1));q.setString(2,col.group(2));try(var r=q.executeQuery()){r.next();exists=r.getInt(1)>0;}}}
    else if(key.find()){try(var q=c.prepareStatement("SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name=? AND constraint_name=?")){q.setString(1,key.group(1));q.setString(2,key.group(2));try(var r=q.executeQuery()){r.next();exists=r.getInt(1)>0;}}}
    if(exists){skipped++;continue;}s.execute(part);applied++;
   }
   System.out.println("Workflow migration applied: "+applied+" statements; "+skipped+" existing columns/constraints preserved.");
  }finally{com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();}
 }
 static String quote(String value){return "\""+value.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r")+"\"";}
}
