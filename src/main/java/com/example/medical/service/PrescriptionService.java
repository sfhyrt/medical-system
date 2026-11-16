package com.example.medical.service;

import com.example.medical.dto.WorkflowRequests;
import com.example.medical.entity.*;
import com.example.medical.exception.*;
import com.example.medical.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PrescriptionService {
    private final RegistrationMapper registrations;
    private final PrescriptionMapper prescriptions;
    private final PrescriptionItemMapper items;
    private final InventoryService inventory;
    public PrescriptionService(RegistrationMapper registrations, PrescriptionMapper prescriptions, PrescriptionItemMapper items, InventoryService inventory){
        this.registrations=registrations;this.prescriptions=prescriptions;this.items=items;this.inventory=inventory;
    }
    @Transactional(rollbackFor=Exception.class)
    public Prescription issue(WorkflowRequests.Issue request){
        var registration=registrations.lock(request.registrationId());if(registration==null)throw new ResourceNotFoundException();
        if(!"BOOKED".equals(registration.getStatus()))throw new BusinessException("REGISTRATION_NOT_BOOKED","该挂号已取消或已完成，不能重复开方");
        var sorted=new ArrayList<>(request.items());sorted.sort(Comparator.comparing(WorkflowRequests.Item::medicineId));
        if(sorted.stream().map(WorkflowRequests.Item::medicineId).distinct().count()!=sorted.size())throw new BusinessException("DUPLICATE_MEDICINE","同一处方中的药品不能重复，请合并数量");
        var prescription=new Prescription();prescription.setPrescriptionNo("RX"+UUID.randomUUID().toString().replace("-",""));
        prescription.setRegistrationId(registration.getId());prescription.setDiagnosis(request.diagnosis().trim());prescription.setIssuedAt(LocalDateTime.now());
        prescriptions.insert(prescription);
        int line=0;
        // Stable lock ordering prevents two multi-item prescriptions from locking inventory in opposite orders.
        for(var input:sorted){
            inventory.consume(input.medicineId(),input.quantity());
            var item=new PrescriptionItem();item.setPrescriptionId(prescription.getId());item.setLineNo(++line);item.setMedicineId(input.medicineId());
            item.setDosage(input.dosage().trim());item.setFrequency(input.frequency().trim());item.setDays(input.days());item.setQuantity(input.quantity());items.insert(item);
        }
        registration.setStatus("COMPLETED");registrations.updateById(registration);return prescription;
    }
}
