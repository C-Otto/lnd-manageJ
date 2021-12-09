package de.cotto.lndmanagej.dto;

import de.cotto.lndmanagej.controller.dto.SelfPaymentDto;

import java.util.List;

public record SelfPaymentsDto(List<SelfPaymentDto> selfPayments) {
}
