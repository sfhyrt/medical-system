package com.example.medical;

import com.example.medical.dto.WorkflowRequests.*;
import com.example.medical.exception.BusinessException;
import com.example.medical.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
@EnabledIfSystemProperty(named="transaction.check",matches="true")
class WorkflowTransactionTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired ScheduleService schedules;
    @Autowired RegistrationService bookings;
    @Autowired PrescriptionService prescriptions;
    @MockitoSpyBean InventoryService inventory;
    @MockitoSpyBean com.example.medical.mapper.DoctorScheduleMapper quotaMapper;
    long department,doctor,patient1,patient2,medicine1,medicine2,schedule;
    String prefix;

    @BeforeEach void prepare(){
        reset(inventory,quotaMapper);prefix="TX"+UUID.randomUUID().toString().replace("-","").substring(0,12);
        jdbc.update("INSERT INTO department(code,name,location,phone) VALUES(?,?,?,?)",prefix,prefix,"Synthetic site","13900000000");
        department=id("department","code",prefix);
        jdbc.update("INSERT INTO doctor(code,name,department_id,title,phone,status) VALUES(?,?,?,?,?,1)",prefix,"Synthetic doctor",department,"Synthetic title","13900000000");doctor=id("doctor","code",prefix);
        jdbc.update("INSERT INTO patient(name,id_card,phone) VALUES(?,?,?)",prefix+"A",identity(),"13900000000");patient1=id("patient","name",prefix+"A");
        jdbc.update("INSERT INTO patient(name,id_card,phone) VALUES(?,?,?)",prefix+"B",identity(),"13900000000");patient2=id("patient","name",prefix+"B");
        jdbc.update("INSERT INTO medicine(code,name,unit,stock) VALUES(?,?,?,10)",prefix+"A","Synthetic A","box");medicine1=id("medicine","code",prefix+"A");
        jdbc.update("INSERT INTO medicine(code,name,unit,stock) VALUES(?,?,?,1)",prefix+"B","Synthetic B","box");medicine2=id("medicine","code",prefix+"B");
        schedule=schedules.create(new Schedule(doctor,LocalDate.now().plusDays(2),"AM",3)).getId();
    }
    private String identity(){return "9"+String.format("%017d",ThreadLocalRandom.current().nextLong(10000000000000000L,99999999999999999L));}
    private long id(String table,String column,String value){return jdbc.queryForObject("SELECT id FROM "+table+" WHERE "+column+"=?",Long.class,value);}
    private long count(String sql,Object... args){return jdbc.queryForObject(sql,Long.class,args);}
    private BigDecimal stock(long id){return jdbc.queryForObject("SELECT stock FROM medicine WHERE id=?",BigDecimal.class,id);}
    private String status(long id){return jdbc.queryForObject("SELECT status FROM registration WHERE id=?",String.class,id);}
    private long remaining(){return count("SELECT remaining FROM doctor_schedule WHERE id=?",schedule);}
    private Item item(long id,int qty){return new Item(id,"Synthetic dose","Synthetic frequency",1,BigDecimal.valueOf(qty));}
    private Issue issue(long registration,Item... items){return new Issue(registration,"Synthetic diagnosis",List.of(items));}
    private void noPrescription(long registration){assertEquals(0,count("SELECT COUNT(*) FROM prescription WHERE registration_id=?",registration));assertEquals("BOOKED",status(registration));}
    private void evidence(String name,long registration){System.out.printf("TRANSACTION_EVIDENCE %s remaining=%d prescriptions=%d items=%d stockA=%s stockB=%s registration=%s%n",name,remaining(),count("SELECT COUNT(*) FROM prescription WHERE registration_id=?",registration),count("SELECT COUNT(*) FROM prescription_item i JOIN prescription p ON p.id=i.prescription_id WHERE p.registration_id=?",registration),stock(medicine1),stock(medicine2),status(registration));}

    @AfterEach void cleanup(){
        reset(inventory,quotaMapper);
        jdbc.update("DELETE i FROM prescription_item i JOIN prescription pr ON pr.id=i.prescription_id JOIN registration r ON r.id=pr.registration_id WHERE r.doctor_id=?",doctor);
        jdbc.update("DELETE pr FROM prescription pr JOIN registration r ON r.id=pr.registration_id WHERE r.doctor_id=?",doctor);
        jdbc.update("DELETE FROM registration WHERE doctor_id=?",doctor);jdbc.update("DELETE FROM doctor_schedule WHERE doctor_id=?",doctor);
        jdbc.update("DELETE FROM medicine WHERE id IN (?,?)",medicine1,medicine2);jdbc.update("DELETE FROM patient WHERE id IN (?,?)",patient1,patient2);
        jdbc.update("DELETE FROM doctor WHERE id=?",doctor);jdbc.update("DELETE FROM department WHERE id=?",department);
    }

    @Test void bookingCancellationAndRebookingRestoreExactlyOneSlot(){
        var first=bookings.book(new Booking(patient1,schedule));assertEquals(2,remaining());
        assertEquals("DUPLICATE_BOOKING",assertThrows(BusinessException.class,()->bookings.book(new Booking(patient1,schedule))).getCode());assertEquals(2,remaining());
        bookings.cancel(first.getId());bookings.cancel(first.getId());assertEquals(3,remaining());
        var second=bookings.book(new Booking(patient1,schedule));assertNotEquals(first.getId(),second.getId());assertEquals(2,remaining());
        System.out.println("TRANSACTION_EVIDENCE cancel_rebook duplicate_rejected=true repeated_cancel_no_extra_slot=true remaining=2");
    }

    @Test void injectedFailureAfterRealQuotaDeductionRestoresQuota() {
        var forward=mockingDetails(quotaMapper).getMockCreationSettings().getDefaultAnswer();
        doAnswer(call->{forward.answer(call);throw new IllegalStateException("Injected after real quota update");}).when(quotaMapper).reserve(schedule);
        assertThrows(IllegalStateException.class,()->bookings.book(new Booking(patient1,schedule)));
        assertEquals(3,remaining());assertEquals(0,count("SELECT COUNT(*) FROM registration WHERE schedule_id=?",schedule));
        System.out.println("TRANSACTION_EVIDENCE booking_failure before_remaining=3 after_remaining=3 registrations=0");
    }

    @Test void successfulPrescriptionCommitsHeaderItemsStockAndStatus(){
        var reg=bookings.book(new Booking(patient1,schedule));var result=prescriptions.issue(issue(reg.getId(),item(medicine1,2),item(medicine2,1)));
        assertEquals(2,count("SELECT COUNT(*) FROM prescription_item WHERE prescription_id=?",result.getId()));assertEquals(0,stock(medicine1).compareTo(new BigDecimal("8")));assertEquals(0,stock(medicine2).signum());assertEquals("COMPLETED",status(reg.getId()));
        assertThrows(BusinessException.class,()->prescriptions.issue(issue(reg.getId(),item(medicine1,1))));assertThrows(BusinessException.class,()->bookings.cancel(reg.getId()));assertEquals(2,remaining());evidence("commit_success",reg.getId());
    }

    @Test void insufficientSecondMedicineRollsBackEarlierStockHeaderAndItem(){
        var reg=bookings.book(new Booking(patient1,schedule));evidence("low_stock_before",reg.getId());
        assertEquals("INSUFFICIENT_STOCK",assertThrows(BusinessException.class,()->prescriptions.issue(issue(reg.getId(),item(medicine1,2),item(medicine2,2)))).getCode());
        noPrescription(reg.getId());assertEquals(0,stock(medicine1).compareTo(BigDecimal.TEN));assertEquals(0,stock(medicine2).compareTo(BigDecimal.ONE));evidence("low_stock_after",reg.getId());
    }

    @Test void injectedFailureAfterRealDeductionRollsBackWholePrescription(){
        var reg=bookings.book(new Booking(patient1,schedule));evidence("injected_failure_before",reg.getId());
        InventoryService target=org.springframework.test.util.AopTestUtils.getUltimateTargetObject(inventory);
        doAnswer(call->{call.callRealMethod();throw new IllegalStateException("Injected after real inventory update");}).when(target).consume(eq(medicine1),any(BigDecimal.class));
        assertThrows(IllegalStateException.class,()->prescriptions.issue(issue(reg.getId(),item(medicine1,2))));
        noPrescription(reg.getId());assertEquals(0,stock(medicine1).compareTo(BigDecimal.TEN));evidence("injected_failure_after",reg.getId());
        reset(inventory);prescriptions.issue(issue(reg.getId(),item(medicine1,2)));assertEquals("COMPLETED",status(reg.getId()));
    }

    @Test void duplicateMedicineAndCancelledRegistrationCannotChangeStock(){
        var reg=bookings.book(new Booking(patient1,schedule));
        assertEquals("DUPLICATE_MEDICINE",assertThrows(BusinessException.class,()->prescriptions.issue(issue(reg.getId(),item(medicine1,1),item(medicine1,1)))).getCode());
        bookings.cancel(reg.getId());assertThrows(BusinessException.class,()->prescriptions.issue(issue(reg.getId(),item(medicine1,1))));assertEquals(0,stock(medicine1).compareTo(BigDecimal.TEN));assertEquals(3,remaining());
    }

    @Test void capacityCannotShrinkBelowUsedAndDisabledScheduleCannotBook(){
        bookings.book(new Booking(patient1,schedule));bookings.book(new Booking(patient2,schedule));
        assertEquals("CAPACITY_TOO_SMALL",assertThrows(BusinessException.class,()->schedules.update(schedule,new ScheduleUpdate(1,1))).getCode());assertEquals(1,remaining());
        schedules.update(schedule,new ScheduleUpdate(4,0));assertEquals(2,remaining());
        assertEquals("SCHEDULE_DISABLED",assertThrows(BusinessException.class,()->bookings.book(new Booking(patient1,schedule))).getCode());
    }

    @Test void expiredScheduleAndInactiveDoctorRejectBooking(){
        jdbc.update("UPDATE doctor_schedule SET visit_date=DATE_SUB(CURRENT_DATE,INTERVAL 1 DAY) WHERE id=?",schedule);
        assertEquals("SCHEDULE_EXPIRED",assertThrows(BusinessException.class,()->bookings.book(new Booking(patient1,schedule))).getCode());
        jdbc.update("UPDATE doctor_schedule SET visit_date=DATE_ADD(CURRENT_DATE,INTERVAL 2 DAY) WHERE id=?",schedule);jdbc.update("UPDATE doctor SET status=0 WHERE id=?",doctor);
        assertEquals("DOCTOR_UNAVAILABLE",assertThrows(BusinessException.class,()->bookings.book(new Booking(patient1,schedule))).getCode());assertEquals(3,remaining());
    }

    private List<String> race(List<Callable<String>> jobs) throws Exception {
        try(var pool=Executors.newFixedThreadPool(jobs.size())){var ready=new CountDownLatch(jobs.size());var go=new CountDownLatch(1);var futures=new ArrayList<Future<String>>();
            for(var job:jobs)futures.add(pool.submit(()->{ready.countDown();go.await(5,TimeUnit.SECONDS);return job.call();}));assertTrue(ready.await(5,TimeUnit.SECONDS));go.countDown();var results=new ArrayList<String>();for(var f:futures)results.add(f.get(20,TimeUnit.SECONDS));return results;}
    }
    private String attempt(long patient){try{bookings.book(new Booking(patient,schedule));return "OK";}catch(BusinessException e){return e.getCode();}}
    @Test void simultaneousBookingsForLastSlotCannotOversell() throws Exception {
        schedules.update(schedule,new ScheduleUpdate(1,1));var results=race(List.of(()->attempt(patient1),()->attempt(patient2)));
        assertEquals(1,Collections.frequency(results,"OK"));assertEquals(1,Collections.frequency(results,"SCHEDULE_FULL"));assertEquals(0,remaining());assertEquals(1,count("SELECT COUNT(*) FROM registration WHERE schedule_id=?",schedule));System.out.println("TRANSACTION_EVIDENCE last_slot_race results="+results+" remaining=0 active=1");
    }
    @Test void simultaneousSamePatientCreatesOneRegistration() throws Exception {
        var results=race(List.of(()->attempt(patient1),()->attempt(patient1),()->attempt(patient1)));
        assertEquals(1,Collections.frequency(results,"OK"));assertEquals(2,Collections.frequency(results,"DUPLICATE_BOOKING"));assertEquals(2,remaining());System.out.println("TRANSACTION_EVIDENCE duplicate_race results="+results+" remaining=2 active=1");
    }
    @Test void concurrentPrescriptionsCannotDeductSameInventoryTwice() throws Exception {
        var a=bookings.book(new Booking(patient1,schedule));var b=bookings.book(new Booking(patient2,schedule));
        Callable<String> first=()->{try{prescriptions.issue(issue(a.getId(),item(medicine2,1)));return "OK";}catch(BusinessException e){return e.getCode();}};
        Callable<String> second=()->{try{prescriptions.issue(issue(b.getId(),item(medicine2,1)));return "OK";}catch(BusinessException e){return e.getCode();}};
        var results=race(List.of(first,second));assertEquals(1,Collections.frequency(results,"OK"));assertEquals(1,Collections.frequency(results,"INSUFFICIENT_STOCK"));assertEquals(0,stock(medicine2).signum());System.out.println("TRANSACTION_EVIDENCE inventory_race results="+results+" stock=0");
    }
    @Test void concurrentCancellationAndPrescriptionHaveOneValidFinalState() throws Exception {
        var reg=bookings.book(new Booking(patient1,schedule));
        var results=race(List.of(()->{try{bookings.cancel(reg.getId());return "CANCELLED";}catch(BusinessException e){return e.getCode();}},()->{try{prescriptions.issue(issue(reg.getId(),item(medicine1,2)));return "ISSUED";}catch(BusinessException e){return e.getCode();}}));
        if("CANCELLED".equals(status(reg.getId()))){assertEquals(3,remaining());assertEquals(0,stock(medicine1).compareTo(BigDecimal.TEN));assertEquals(0,count("SELECT COUNT(*) FROM prescription WHERE registration_id=?",reg.getId()));}
        else {assertEquals("COMPLETED",status(reg.getId()));assertEquals(2,remaining());assertEquals(0,stock(medicine1).compareTo(new BigDecimal("8")));assertEquals(1,count("SELECT COUNT(*) FROM prescription WHERE registration_id=?",reg.getId()));}
        System.out.println("TRANSACTION_EVIDENCE cancellation_issue_race results="+results+" final="+status(reg.getId()));
    }
}
