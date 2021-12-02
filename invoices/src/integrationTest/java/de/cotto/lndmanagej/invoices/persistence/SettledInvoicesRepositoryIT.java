package de.cotto.lndmanagej.invoices.persistence;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SettledInvoicesRepositoryIT {
    @Autowired
    private SettledInvoicesRepository repository;

    @Test
    void getMaxSettledIndex_no_invoice() {
        assertThat(repository.getMaxSettledIndex()).isEqualTo(0);
    }

    @Test
    void getMaxSettledIndex_with_gaps_in_addIndex() {
        repository.save(invoice(1, 1));
        repository.save(invoice(2, 2));
        repository.save(invoice(5, 3));
        repository.save(invoice(6, 5));
        repository.save(invoice(7, 4));
        assertThat(repository.getMaxSettledIndex()).isEqualTo(5);
    }

    @Test
    void getMaxAddIndexWithoutGaps_no_invoice() {
        assertThat(repository.getMaxAddIndexWithoutGaps()).isEqualTo(0);
    }

    @Test
    void getMaxAddIndexWithoutGaps_no_gap() {
        repository.save(invoice(1, 1));
        repository.save(invoice(2, 2));
        repository.save(invoice(3, 3));
        assertThat(repository.getMaxAddIndexWithoutGaps()).isEqualTo(3L);
    }

    @Test
    void getMaxAddIndexWithoutGaps_gap() {
        repository.save(invoice(1, 1));
        repository.save(invoice(2, 2));
        repository.save(invoice(5, 3));
        repository.save(invoice(6, 6));
        repository.save(invoice(7, 5));
        assertThat(repository.getMaxAddIndexWithoutGaps()).isEqualTo(5L);
    }

    private SettledInvoiceJpaDto invoice(int addIndex, int settleIndex) {
        return SettledInvoiceJpaDto.createFromInvoice(
                new SettledInvoice(addIndex, settleIndex, LocalDateTime.MIN, "", Coins.NONE, "")
        );
    }
}