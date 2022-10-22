package de.cotto.lndmanagej.invoices.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SettledInvoicesIndexRepositoryIT {
    private static final long ID_OF_SETTLED_INDEX_ENTITY = 0L;

    @Autowired
    private SettledInvoicesIndexRepository repository;

    @Test
    void no_entity() {
        assertThat(repository.findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)).isEmpty();
    }

    @Test
    void saved_entity() {
        SettledInvoicesIndexJpaDto entity = new SettledInvoicesIndexJpaDto();
        entity.setEntityId(ID_OF_SETTLED_INDEX_ENTITY);
        entity.setAllSettledIndexOffset(123);
        repository.save(entity);
        assertThat(repository.findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)
                .map(SettledInvoicesIndexJpaDto::getAllSettledIndexOffset)
        ).contains(123L);
    }

    @Test
    void updated_entity() {
        SettledInvoicesIndexJpaDto entity = new SettledInvoicesIndexJpaDto();
        entity.setEntityId(ID_OF_SETTLED_INDEX_ENTITY);
        entity.setAllSettledIndexOffset(123);
        repository.save(entity);
        entity.setAllSettledIndexOffset(456);
        repository.save(entity);
        assertThat(repository.findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)
                .map(SettledInvoicesIndexJpaDto::getAllSettledIndexOffset)
        ).contains(456L);
    }
}
