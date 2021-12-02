package de.cotto.lndmanagej.invoices;

import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettledInvoicesTest {
    private static final long OFFSET = 1_234;

    @InjectMocks
    private SettledInvoices settledInvoices;

    @Mock
    private GrpcInvoices grpcInvoices;

    @Mock
    private SettledInvoicesDao dao;

    @BeforeEach
    void setUp() {
        when(dao.getOffset()).thenReturn(OFFSET);
        when(grpcInvoices.getLimit()).thenReturn(1);
    }

    @Test
    void refresh_uses_known_offset() {
        settledInvoices.refresh();
        verify(grpcInvoices).getSettledInvoicesAfter(OFFSET);
    }

    @Test
    void refresh_saves_invoices() {
        when(grpcInvoices.getSettledInvoicesAfter(OFFSET)).thenReturn(
                Optional.of(List.of(SETTLED_INVOICE, SETTLED_INVOICE_2))
        ).thenReturn(Optional.of(List.of()));
        settledInvoices.refresh();
        verify(dao).save(List.of(SETTLED_INVOICE, SETTLED_INVOICE_2));
    }

    @Test
    void refresh_ignores_invalid_invoices() {
        when(grpcInvoices.getSettledInvoicesAfter(OFFSET)).thenReturn(
                Optional.of(List.of(SETTLED_INVOICE, SettledInvoice.INVALID, SETTLED_INVOICE_2, SettledInvoice.INVALID))
        ).thenReturn(Optional.of(List.of()));
        settledInvoices.refresh();
        verify(dao).save(List.of(SETTLED_INVOICE, SETTLED_INVOICE_2));
    }

    @Test
    void refresh_repeats_while_at_limit() {
        when(grpcInvoices.getSettledInvoicesAfter(OFFSET))
                .thenReturn(Optional.of(List.of(SETTLED_INVOICE)))
                .thenReturn(Optional.of(List.of(SETTLED_INVOICE_2)))
                .thenReturn(Optional.of(List.of()));
        settledInvoices.refresh();
        verify(dao).save(List.of(SETTLED_INVOICE));
        verify(dao).save(List.of(SETTLED_INVOICE_2));
    }
}