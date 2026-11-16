package com.example.medical.service;

import com.example.medical.exception.*;
import com.example.medical.mapper.MedicineMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.math.BigDecimal;

@Service
public class InventoryService {
    private final MedicineMapper medicines;
    public InventoryService(MedicineMapper medicines){this.medicines=medicines;}
    @Transactional(propagation=Propagation.MANDATORY, rollbackFor=Exception.class)
    public void consume(long medicineId, BigDecimal quantity){
        if(quantity==null||quantity.signum()<=0)throw new IllegalArgumentException("Quantity must be positive");
        if(medicines.consume(medicineId,quantity)!=1){
            if(medicines.selectById(medicineId)==null)throw new ResourceNotFoundException();
            throw new BusinessException("INSUFFICIENT_STOCK","药品库存不足，处方未保存，已扣库存会自动恢复");
        }
    }
}
