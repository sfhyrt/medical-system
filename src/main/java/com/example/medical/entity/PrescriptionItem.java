package com.example.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author Medical System
 * @since 2026-11-16
 */
@TableName("prescription_item")
public class PrescriptionItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("prescription_id")
    private Long prescriptionId;

    @TableField("line_no")
    private Integer lineNo;

    @TableField("medicine_id")
    private Long medicineId;

    @TableField("dosage")
    private String dosage;

    @TableField("frequency")
    private String frequency;

    @TableField("days")
    private Integer days;

    @TableField("quantity")
    private BigDecimal quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "PrescriptionItem{" +
            "id = " + id +
            ", prescriptionId = " + prescriptionId +
            ", lineNo = " + lineNo +
            ", medicineId = " + medicineId +
            ", dosage = " + dosage +
            ", frequency = " + frequency +
            ", days = " + days +
            ", quantity = " + quantity +
            "}";
    }
}
