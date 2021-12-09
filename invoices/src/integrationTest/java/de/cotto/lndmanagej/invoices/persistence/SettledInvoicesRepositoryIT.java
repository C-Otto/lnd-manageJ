package de.cotto.lndmanagej.invoices.persistence;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SettledInvoicesRepositoryIT {
    @Autowired
    private SettledInvoicesRepository repository;

    @Test
    void getMaxSettledIndexWithoutGaps_no_invoice() {
        assertThat(repository.getMaxSettledIndexWithoutGaps()).isEqualTo(0);
    }

    @Test
    void getMaxSettledIndexWithoutGaps_with_gaps_in_addIndex() {
        repository.save(invoice(1, 1));
        repository.save(invoice(2, 2));
        repository.save(invoice(5, 3));
        repository.save(invoice(6, 5));
        repository.save(invoice(7, 4));
        assertThat(repository.getMaxSettledIndexWithoutGaps()).isEqualTo(5);
    }

    @Test
    void getMaxSettleIndexWithoutGaps_gap_in_settleIndex() {
        repository.save(invoice(1, 1));
        repository.save(invoice(2, 2));
        repository.save(invoice(5, 3));
        repository.save(invoice(6, 6));
        repository.save(invoice(7, 5));
        assertThat(repository.getMaxSettledIndexWithoutGaps()).isEqualTo(3L);
    }

    @Test
    void getMaxAddIndex_no_invoice() {
        assertThat(repository.getMaxAddIndex()).isEqualTo(0);
    }

    @Test
    void getMaxAddIndex() {
        repository.save(invoice(1, 1));
        repository.save(invoice(2, 2));
        repository.save(invoice(3, 4));
        assertThat(repository.getMaxAddIndex()).isEqualTo(3L);
    }

    private SettledInvoiceJpaDto invoice(int addIndex, int settleIndex) {
        return SettledInvoiceJpaDto.createFromModel(new SettledInvoice(
                        addIndex,
                        settleIndex,
                        LocalDateTime.MIN.atZone(ZoneOffset.UTC),
                        "",
                        Coins.NONE,
                        "",
                        Optional.empty(),
                        Optional.of(CHANNEL_ID)
                )
        );
    }
}