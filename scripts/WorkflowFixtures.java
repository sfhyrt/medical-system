import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Isolated synthetic fixtures shared by HTTP and browser verification. */
class WorkflowFixtures {
    static long id(Connection c,String table,String field,String value)throws SQLException {
        try(var q=c.prepareStatement("SELECT id FROM "+table+" WHERE "+field+"=?")){q.setString(1,value);try(var r=q.executeQuery()){if(!r.next())throw new SQLException("Fixture missing");return r.getLong(1);}}
    }
    static void write(Connection c,String sql,Object... args)throws SQLException {
        try(var q=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)q.setObject(i+1,args[i]);q.executeUpdate();}
    }
    static String identity(){return "9"+String.format("%017d",java.util.concurrent.ThreadLocalRandom.current().nextLong(10000000000000000L,99999999999999999L));}
    public static void main(String[] args)throws Exception {
        if(args.length!=2||!args[1].matches("WF_[A-F0-9]{12}"))throw new IllegalArgumentException("Use setup/cleanup and a WF_ synthetic identifier");
        String prefix=args[1];Properties p=new Properties();Path config=Path.of("application-local.properties");
        if(Files.exists(config))try(var in=Files.newInputStream(config)){p.load(in);}
        String url=p.getProperty("spring.datasource.url",System.getenv().getOrDefault("DB_URL","jdbc:mysql://127.0.0.1:3306/medical_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"));
        try(Connection c=DriverManager.getConnection(url,p.getProperty("spring.datasource.username",System.getenv().getOrDefault("DB_USERNAME","root")),p.getProperty("spring.datasource.password",System.getenv().getOrDefault("DB_PASSWORD","")))){
            c.setAutoCommit(false);
            if(args[0].equals("setup")){
                write(c,"INSERT INTO department(code,name,location,phone) VALUES(?,?,?,?)",prefix,prefix,"Synthetic location","13900000000");long dep=id(c,"department","code",prefix);
                write(c,"INSERT INTO doctor(code,name,department_id,title,phone,status) VALUES(?,?,?,?,?,1)",prefix,"流程预览医生",dep,"模拟职称","13900000000");long doctor=id(c,"doctor","code",prefix);
                write(c,"INSERT INTO patient(name,id_card,phone) VALUES(?,?,?)",prefix+"A",identity(),"13900000000");long patient1=id(c,"patient","name",prefix+"A");
                write(c,"INSERT INTO patient(name,id_card,phone) VALUES(?,?,?)",prefix+"B",identity(),"13900000000");long patient2=id(c,"patient","name",prefix+"B");
                write(c,"INSERT INTO medicine(code,name,unit,stock) VALUES(?,?,?,10)",prefix+"A","流程模拟药品 A","盒");long medicine1=id(c,"medicine","code",prefix+"A");
                write(c,"INSERT INTO medicine(code,name,unit,stock) VALUES(?,?,?,1)",prefix+"B","流程模拟药品 B","瓶");long medicine2=id(c,"medicine","code",prefix+"B");
                c.commit();System.out.printf("{\"department\":%d,\"doctor\":%d,\"patient1\":%d,\"patient2\":%d,\"medicine1\":%d,\"medicine2\":%d}%n",dep,doctor,patient1,patient2,medicine1,medicine2);
            } else if(args[0].equals("cleanup")){
                long dep=id(c,"department","code",prefix),doctor=id(c,"doctor","code",prefix);
                write(c,"DELETE i FROM prescription_item i JOIN prescription pr ON pr.id=i.prescription_id JOIN registration r ON r.id=pr.registration_id WHERE r.doctor_id=?",doctor);
                write(c,"DELETE pr FROM prescription pr JOIN registration r ON r.id=pr.registration_id WHERE r.doctor_id=?",doctor);
                write(c,"DELETE FROM registration WHERE doctor_id=?",doctor);write(c,"DELETE FROM doctor_schedule WHERE doctor_id=?",doctor);
                write(c,"DELETE FROM medicine WHERE code IN (?,?)",prefix+"A",prefix+"B");write(c,"DELETE FROM patient WHERE name IN (?,?)",prefix+"A",prefix+"B");
                write(c,"DELETE FROM doctor WHERE id=? AND code=?",doctor,prefix);write(c,"DELETE FROM department WHERE id=? AND code=? AND name=?",dep,prefix,prefix);c.commit();System.out.println("Synthetic fixture removed: "+prefix);
            }else throw new IllegalArgumentException("Unknown operation");
        }
    }
}
