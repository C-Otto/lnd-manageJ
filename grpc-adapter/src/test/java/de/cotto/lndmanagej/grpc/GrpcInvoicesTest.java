package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.SettledInvoice;
import lnrpc.Invoice;
import lnrpc.ListInvoiceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static lnrpc.Invoice.InvoiceState.ACCEPTED;
import static lnrpc.Invoice.InvoiceState.OPEN;
import static lnrpc.Invoice.InvoiceState.SETTLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcInvoicesTest {
    private static final long OFFSET = 123;
    private static final int LIMIT = 1_000;
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    @InjectMocks
    private GrpcInvoices grpcInvoices;

    @Mock
    private GrpcService grpcService;

    @Test
    void empty_optional() {
        when(grpcService.getInvoices(anyLong(), anyInt())).thenReturn(Optional.empty());
        assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).isEmpty();
    }

    @Test
    void no_invoice() {
        ListInvoiceResponse response = ListInvoiceResponse.newBuilder().build();
        when(grpcService.getInvoices(anyLong(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(List.of());
    }

    @Test
    void with_events() {
        ListInvoiceResponse response = ListInvoiceResponse.newBuilder()
                .addInvoices(invoice(SETTLED, SETTLED_INVOICE))
                .addInvoices(invoice(SETTLED, SETTLED_INVOICE_2))
                .build();
        when(grpcService.getInvoices(anyLong(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                List.of(SETTLED_INVOICE, SETTLED_INVOICE_2)
        );
    }

    @Test
    void returns_non_settled_invoices_as_invalid() {
        ListInvoiceResponse response = ListInvoiceResponse.newBuilder()
                .addInvoices(invoice(ACCEPTED, null))
                .addInvoices(invoice(OPEN, null))
                .build();
        when(grpcService.getInvoices(anyLong(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                List.of(SettledInvoice.INVALID, SettledInvoice.INVALID)
        );
    }

    @Test
    void starts_at_the_beginning() {
        grpcInvoices.getSettledInvoicesAfter(OFFSET);
        verify(grpcService).getInvoices(eq(OFFSET), anyInt());
    }

    @Test
    void uses_limit() {
        grpcInvoices.getSettledInvoicesAfter(OFFSET);
        verify(grpcService).getInvoices(OFFSET, LIMIT);
    }

    @Test
    void getLimit() {
        assertThat(grpcInvoices.getLimit()).isEqualTo(LIMIT);
    }

    private Invoice invoice(Invoice.InvoiceState state, @Nullable SettledInvoice settledInvoice) {
        if (settledInvoice == null) {
            return Invoice.newBuilder().setState(state).build();
        }
        return Invoice.newBuilder()
                .setState(state)
                .setAddIndex(settledInvoice.addIndex())
                .setSettleIndex(settledInvoice.settleIndex())
                .setRHash(ByteString.copyFrom(HEX_FORMAT.parseHex(settledInvoice.hash())))
                .setMemo(settledInvoice.memo())
                .setSettleDate(settledInvoice.settleDate().toEpochSecond(ZoneOffset.UTC))
                .setAmtPaidMsat(settledInvoice.amountPaid().milliSatoshis())
                .build();
    }
}