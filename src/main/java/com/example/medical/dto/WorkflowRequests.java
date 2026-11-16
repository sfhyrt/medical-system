package com.example.medical.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class WorkflowRequests {
    private WorkflowRequests() {}
    public record Schedule(@NotNull @Positive Long doctorId, @NotNull @FutureOrPresent LocalDate visitDate,
                           @NotBlank @Pattern(regexp="AM|PM") String session, @NotNull @Min(1) @Max(1000) Integer capacity) {}
    public record ScheduleUpdate(@NotNull @Min(1) @Max(1000) Integer capacity, @NotNull @Min(0) @Max(1) Integer enabled) {}
    public record Booking(@NotNull @Positive Long patientId, @NotNull @Positive Long scheduleId) {}
    public record Item(@NotNull @Positive Long medicineId, @NotBlank @Size(max=100) String dosage,
                       @NotBlank @Size(max=100) String frequency, @NotNull @Min(1) @Max(365) Integer days,
                       @NotNull @DecimalMin("0.01") @DecimalMax("99999999.99") @Digits(integer=8,fraction=2) BigDecimal quantity) {}
    public record Issue(@NotNull @Positive Long registrationId, @NotBlank @Size(max=500) String diagnosis,
                        @NotEmpty @Size(max=20) List<@NotNull @Valid Item> items) {}
}
