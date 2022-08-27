package de.cotto.lndmanagej.invoices.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
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

    @Test
    void findAllByReceivedViaChannelIdAndSettleDateAfter_no_invoice() {
        assertThat(repository.findAllByReceivedViaChannelIdAndSettleDateAfter(CHANNEL_ID.getShortChannelId(), 0))
                .isEmpty();
    }

    @Test
    void findAllByReceivedViaChannelIdAndSettleDateAfter_wrong_channel_id() {
        repository.save(invoice(1, 1, Map.of(CHANNEL_ID, Coins.ofSatoshis(100))));
        assertThat(repository.findAllByReceivedViaChannelIdAndSettleDateAfter(CHANNEL_ID_2.getShortChannelId(), 0))
                .isEmpty();
    }

    @Test
    void findAllByReceivedViaChannelIdAndSettleDateAfter_expected_channel_id() {
        Map<ChannelId, Coins> expected = Map.of(CHANNEL_ID, Coins.ofSatoshis(100));
        repository.save(invoice(1, 1, expected));
        assertReceivedVia(CHANNEL_ID, List.of(expected));
    }

    @Test
    void findAllByReceivedViaChannelIdAndSettleDateAfter_just_within_max_age() {
        Map<ChannelId, Coins> tooOld = Map.of(CHANNEL_ID, Coins.ofSatoshis(100));
        SettledInvoiceJpaDto invoice = invoice(1, 1, tooOld);
        repository.save(invoice);
        long timestamp = invoice.getSettleDate() - 1;
        List<SettledInvoiceJpaDto> invoices =
                repository.findAllByReceivedViaChannelIdAndSettleDateAfter(CHANNEL_ID.getShortChannelId(), timestamp);
        assertThat(invoices).isNotEmpty();
    }

    @Test
    void findAllByReceivedViaChannelIdAndSettleDateAfter_expected_channel_id_but_too_old() {
        Map<ChannelId, Coins> tooOld = Map.of(CHANNEL_ID, Coins.ofSatoshis(100));
        SettledInvoiceJpaDto invoice = invoice(1, 1, tooOld);
        repository.save(invoice);
        long timestamp = invoice.getSettleDate();
        List<SettledInvoiceJpaDto> invoices =
                repository.findAllByReceivedViaChannelIdAndSettleDateAfter(CHANNEL_ID.getShortChannelId(), timestamp);
        assertThat(invoices).isEmpty();
    }

    @Test
    void findAllByReceivedViaChannelIdAndSettleDateAfter_one_of_two_channels_matches() {
        Map<ChannelId, Coins> expected = Map.of(CHANNEL_ID, Coins.ofSatoshis(100), CHANNEL_ID_2, Coins.ofSatoshis(50));
        repository.save(invoice(1, 1, expected));
        assertReceivedVia(CHANNEL_ID, List.of(expected));
    }

    @Test
    void findAllByReceivedViaChannelIdAndSettleDateAfter_several_invoices() {
        Map<ChannelId, Coins> expected1 = Map.of(CHANNEL_ID, Coins.ofSatoshis(100), CHANNEL_ID_2, Coins.ofSatoshis(50));
        Map<ChannelId, Coins> expected2 = Map.of(CHANNEL_ID_3, Coins.ofSatoshis(3), CHANNEL_ID_2, Coins.ofSatoshis(4));
        repository.save(invoice(1, 1, expected1));
        repository.save(invoice(2, 2, Map.of(CHANNEL_ID, Coins.ofSatoshis(1), CHANNEL_ID_3, Coins.ofSatoshis(2))));
        repository.save(invoice(3, 3, expected2));
        assertReceivedVia(CHANNEL_ID_2, List.of(expected1, expected2));
    }

    private void assertReceivedVia(ChannelId channelId, List<Map<ChannelId, Coins>> expected) {
        assertThat(repository.findAllByReceivedViaChannelIdAndSettleDateAfter(channelId.getShortChannelId(), 0))
                .map(SettledInvoiceJpaDto::toModel)
                .map(SettledInvoice::receivedVia)
                .isEqualTo(expected);
    }

    private SettledInvoiceJpaDto invoice(int addIndex, int settleIndex) {
        return invoice(addIndex, settleIndex, Map.of());
    }

    private SettledInvoiceJpaDto invoice(int addIndex, int settleIndex, Map<ChannelId, Coins> receivedVia) {
        return SettledInvoiceJpaDto.createFromModel(new SettledInvoice(
                        addIndex,
                        settleIndex,
                        LocalDateTime.of(2020, 1, 2, 3, 4).atZone(ZoneOffset.UTC),
                        "",
                        Coins.NONE,
                        "",
                        Optional.empty(),
                        receivedVia
                )
        );
    }
}
