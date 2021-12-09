package de.cotto.lndmanagej.selfpayments.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SelfPaymentsRepositoryTest {
    @Test
    void dummyEntity() {
        // the class is required to define a repository
        assertThat(new SelfPaymentsRepository.DummyEntity()).isNotNull();
    }
}