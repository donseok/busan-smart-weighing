package com.dongkuk.weighing.dispatch.dto;

import com.dongkuk.weighing.dispatch.domain.ItemType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DispatchCreateRequest(
    @NotNull
    Long vehicleId,

    @NotNull
    Long companyId,

    @NotNull
    ItemType itemType,

    @NotBlank @Size(max = 100)
    String itemName,

    @NotNull @FutureOrPresent
    LocalDate dispatchDate,

    @Size(max = 100)
    String originLocation,

    @Size(max = 100)
    String destination,

    String remarks
) {}
