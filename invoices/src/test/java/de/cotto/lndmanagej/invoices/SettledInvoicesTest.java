package de.cotto.lndmanagej.invoices;

import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettledInvoicesTest {
    private static final long ADD_INDEX_OFFSET = 1_234;
    private static final long SETTLE_INDEX_OFFSET = 999;

    @InjectMocks
    private SettledInvoices settledInvoices;

    @Mock
    private GrpcInvoices grpcInvoices;

    @Mock
    private SettledInvoicesDao dao;

    @BeforeEach
    void setUp() {
        when(dao.getAddIndexOffset()).thenReturn(ADD_INDEX_OFFSET);
        when(dao.getSettleIndexOffset()).thenReturn(SETTLE_INDEX_OFFSET);
        when(grpcInvoices.getLimit()).thenReturn(1);
    }

    @Test
    void refresh_uses_known_offset() {
        settledInvoices.refresh();
        verify(grpcInvoices).getSettledInvoicesAfter(ADD_INDEX_OFFSET);
    }

    @Test
    void refresh_saves_invoices() {
        when(grpcInvoices.getSettledInvoicesAfter(ADD_INDEX_OFFSET)).thenReturn(
                Optional.of(List.of(SETTLED_INVOICE, SETTLED_INVOICE_2))
        ).thenReturn(Optional.of(List.of()));
        settledInvoices.refresh();
        verify(dao).save(List.of(SETTLED_INVOICE, SETTLED_INVOICE_2));
    }

    @Test
    void refresh_subscribes_after_bulk_get() {
        settledInvoices.refresh();
        InOrder inOrder = inOrder(grpcInvoices);
        inOrder.verify(grpcInvoices).getSettledInvoicesAfter(ADD_INDEX_OFFSET);
        inOrder.verify(grpcInvoices).getNewSettledInvoicesAfter(SETTLE_INDEX_OFFSET);
    }

    @Test
    void refresh_ignores_invalid_invoices_in_bulk_get() {
        when(grpcInvoices.getSettledInvoicesAfter(ADD_INDEX_OFFSET)).thenReturn(
                Optional.of(List.of(SETTLED_INVOICE, SettledInvoice.INVALID, SETTLED_INVOICE_2, SettledInvoice.INVALID))
        ).thenReturn(Optional.of(List.of()));
        settledInvoices.refresh();
        verify(dao).save(List.of(SETTLED_INVOICE, SETTLED_INVOICE_2));
    }

    @Test
    void refresh_repeats_while_at_limit() {
        when(grpcInvoices.getSettledInvoicesAfter(ADD_INDEX_OFFSET))
                .thenReturn(Optional.of(List.of(SETTLED_INVOICE)))
                .thenReturn(Optional.of(List.of(SETTLED_INVOICE_2)))
                .thenReturn(Optional.of(List.of()));
        settledInvoices.refresh();
        verify(dao).save(List.of(SETTLED_INVOICE));
        verify(dao).save(List.of(SETTLED_INVOICE_2));
    }

    @Test
    void refresh_saves_invoices_from_subscription() {
        when(grpcInvoices.getNewSettledInvoicesAfter(SETTLE_INDEX_OFFSET))
                .thenReturn(Stream.of(SETTLED_INVOICE, SETTLED_INVOICE_2));
        settledInvoices.refresh();
        verify(dao).save(SETTLED_INVOICE);
        verify(dao).save(SETTLED_INVOICE_2);
    }

    @Test
    void refresh_skips_invalid_invoices_from_subscription() {
        when(grpcInvoices.getNewSettledInvoicesAfter(SETTLE_INDEX_OFFSET))
                .thenReturn(Stream.of(SETTLED_INVOICE, SettledInvoice.INVALID, SETTLED_INVOICE_2));
        settledInvoices.refresh();
        verify(dao, times(2)).save(any(SettledInvoice.class));
    }
}