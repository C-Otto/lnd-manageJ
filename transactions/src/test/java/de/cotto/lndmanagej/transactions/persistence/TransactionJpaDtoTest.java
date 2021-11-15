package de.cotto.lndmanagej.transactions.persistence;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static de.cotto.lndmanagej.transactions.persistence.TransactionJpaDtoFixtures.TRANSACTION_JPA_DTO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TransactionJpaDtoTest {

    @Test
    void toModel_nullHash() {
        TransactionJpaDto dto = new TransactionJpaDto();
        assertThat(dto.toModel()).isEmpty();
    }

    @Test
    void toModel() {
        assertThat(TRANSACTION_JPA_DTO.toModel()).contains(TRANSACTION);
    }

    @Test
    void fromModel() {
        TransactionJpaDto fromModel = TransactionJpaDto.fromModel(TRANSACTION);
        assertThat(fromModel).usingRecursiveComparison().isEqualTo(TRANSACTION_JPA_DTO);
    }
}