package de.cotto.lndmanagej.payments.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SettledPaymentIndexRepositoryIT {
    private static final long ID_OF_SETTLED_INDEX_ENTITY = 0L;

    @Autowired
    private SettledPaymentIndexRepository repository;

    @Test
    void no_entity() {
        assertThat(repository.findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)).isEmpty();
    }

    @Test
    void saved_entity() {
        SettledPaymentIndexJpaDto entity = new SettledPaymentIndexJpaDto();
        entity.setEntityId(ID_OF_SETTLED_INDEX_ENTITY);
        entity.setAllSettledIndexOffset(123);
        repository.save(entity);
        assertThat(repository.findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)
                .map(SettledPaymentIndexJpaDto::getAllSettledIndexOffset)
        ).contains(123L);
    }

    @Test
    void updated_entity() {
        SettledPaymentIndexJpaDto entity = new SettledPaymentIndexJpaDto();
        entity.setEntityId(ID_OF_SETTLED_INDEX_ENTITY);
        entity.setAllSettledIndexOffset(123);
        repository.save(entity);
        entity.setAllSettledIndexOffset(456);
        repository.save(entity);
        assertThat(repository.findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)
                .map(SettledPaymentIndexJpaDto::getAllSettledIndexOffset)
        ).contains(456L);
    }
}
