package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import lnrpc.AddInvoiceResponse;
import lnrpc.Invoice;
import lnrpc.InvoiceHTLC;
import lnrpc.InvoiceHTLCState;
import lnrpc.ListInvoiceResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.KEYSEND_MESSAGE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_KEYSEND;
import static lnrpc.Invoice.InvoiceState.ACCEPTED;
import static lnrpc.Invoice.InvoiceState.CANCELED;
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
    private static final int LIMIT = 1_000;
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    @InjectMocks
    private GrpcInvoices grpcInvoices;

    @Mock
    private GrpcService grpcService;

    @Mock
    private GrpcPayments grpcPayments;

    @Nested
    class BulkGet {
        private static final long ADD_INDEX_OFFSET = 123;

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
        void with_invoices() {
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
        void with_keysend_message() {
            mockResponse(keysendInvoice());
            assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                    List.of(SETTLED_INVOICE_KEYSEND)
            );
        }

        @Test
        void with_keysend_message_v2() {
            // https://github.com/alexbosworth/keysend_protocols
            mockResponse(keysendInvoiceV2());
            assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                    List.of(SETTLED_INVOICE_KEYSEND)
            );
        }

        @Test
        void with_keysend_message_just_preimage() {
            LinkedHashMap<Long, ByteString> customRecords = new LinkedHashMap<>();
            customRecords.put(5_482_373_484L, ByteString.copyFromUtf8("000"));
            mockResponse(invoice(SETTLED, SETTLED_INVOICE_KEYSEND, customRecords, List.of()));
            assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                    List.of(SETTLED_INVOICE)
            );
        }

        @Test
        void with_keysend_message_without_preimage() {
            LinkedHashMap<Long, ByteString> customRecords = new LinkedHashMap<>();
            customRecords.put(7_629_168L, ByteString.copyFromUtf8(KEYSEND_MESSAGE));
            mockResponse(invoice(SETTLED, SETTLED_INVOICE_KEYSEND, customRecords, List.of()));
            assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                    List.of(SETTLED_INVOICE)
            );
        }

        @Test
        void received_via_single_channel() {
            SettledInvoice expected = invoiceWithReceivedVia(Map.of(
                    CHANNEL_ID_3, Coins.ofMilliSatoshis(CHANNEL_ID_3.getShortChannelId()))
            );
            mockResponse(settledInvoiceReceivedVia(CHANNEL_ID_3));
            assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                    List.of(expected)
            );
        }

        @Test
        void received_via_two_channels() {
            SettledInvoice expected = invoiceWithReceivedVia(Map.of(
                    CHANNEL_ID_3, Coins.ofMilliSatoshis(CHANNEL_ID_3.getShortChannelId()),
                    CHANNEL_ID_4, Coins.ofMilliSatoshis(CHANNEL_ID_4.getShortChannelId())
            ));
            mockResponse(settledInvoiceReceivedVia(CHANNEL_ID_3, CHANNEL_ID_4));
            assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                    List.of(expected)
            );
        }

        @Test
        void received_via_two_htlcs_in_same_channel() {
            SettledInvoice expected = invoiceWithReceivedVia(Map.of(
                    CHANNEL_ID, Coins.ofMilliSatoshis(CHANNEL_ID.getShortChannelId() * 2)
            ));
            mockResponse(settledInvoiceReceivedVia(CHANNEL_ID, CHANNEL_ID));
            assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                    List.of(expected)
            );
        }

        @Test
        void received_via_single_channel_with_additional_canceled_htlc() {
            List<InvoiceHTLC> htlcs = new ArrayList<>();

            InvoiceHTLC.Builder htlcBuilder = InvoiceHTLC.newBuilder();
            htlcBuilder.setChanId(CHANNEL_ID_2.getShortChannelId());
            htlcBuilder.setAmtMsat(CHANNEL_ID_2.getShortChannelId());
            htlcBuilder.setState(InvoiceHTLCState.CANCELED);
            htlcs.add(htlcBuilder.build());
            htlcBuilder.setChanId(CHANNEL_ID.getShortChannelId());
            htlcBuilder.setAmtMsat(CHANNEL_ID.getShortChannelId());
            htlcBuilder.setState(InvoiceHTLCState.SETTLED);
            htlcs.add(htlcBuilder.build());

            Invoice invoice = Invoice.newBuilder()
                    .setState(SETTLED)
                    .setAddIndex(SETTLED_INVOICE.addIndex())
                    .setSettleIndex(SETTLED_INVOICE.settleIndex())
                    .setRHash(ByteString.copyFrom(HEX_FORMAT.parseHex(SETTLED_INVOICE.hash())))
                    .setMemo(SETTLED_INVOICE.memo())
                    .setSettleDate(SETTLED_INVOICE.settleDate().toEpochSecond())
                    .setAmtPaidMsat(SETTLED_INVOICE.amountPaid().milliSatoshis())
                    .addAllHtlcs(htlcs)
                    .build();
            mockResponse(invoice);
            assertThat(grpcInvoices.getSettledInvoicesAfter(0L)).contains(
                    List.of(SETTLED_INVOICE)
            );
        }

        private void mockResponse(Invoice invoice) {
            when(grpcService.getInvoices(anyLong(), anyInt())).thenReturn(Optional.of(ListInvoiceResponse.newBuilder()
                    .addInvoices(invoice)
                    .build()));
        }

        private SettledInvoice invoiceWithReceivedVia(Map<ChannelId, Coins> receivedVia) {
            return new SettledInvoice(
                    SETTLED_INVOICE.addIndex(),
                    SETTLED_INVOICE.settleIndex(),
                    SETTLED_INVOICE.settleDate(),
                    SETTLED_INVOICE.hash(),
                    SETTLED_INVOICE.amountPaid(),
                    SETTLED_INVOICE.memo(),
                    SETTLED_INVOICE.keysendMessage(),
                    receivedVia
            );
        }

        @Test
        void starts_at_the_beginning() {
            grpcInvoices.getSettledInvoicesAfter(ADD_INDEX_OFFSET);
            verify(grpcService).getInvoices(eq(ADD_INDEX_OFFSET), anyInt());
        }

        @Test
        void uses_limit() {
            grpcInvoices.getSettledInvoicesAfter(ADD_INDEX_OFFSET);
            verify(grpcService).getInvoices(ADD_INDEX_OFFSET, LIMIT);
        }
    }

    @Nested
    class Subscription {
        private static final long SETTLE_INDEX_OFFSET = 999;

        @Test
        void empty() {
            when(grpcService.subscribeToSettledInvoices(anyLong())).thenReturn(Optional.empty());
            assertThat(grpcInvoices.getNewSettledInvoicesAfter(SETTLE_INDEX_OFFSET)).isEmpty();
        }

        @Test
        void one_invoice() {
            when(grpcService.subscribeToSettledInvoices(SETTLE_INDEX_OFFSET))
                    .thenReturn(Optional.of(List.of(invoice(SETTLED, SETTLED_INVOICE)).iterator()));
            assertThat(grpcInvoices.getNewSettledInvoicesAfter(SETTLE_INDEX_OFFSET)).containsExactly(SETTLED_INVOICE);
        }

        @Test
        void two_invoices() {
            List<Invoice> invoices = List.of(
                    invoice(SETTLED, SETTLED_INVOICE),
                    invoice(SETTLED, SETTLED_INVOICE_2)
            );
            when(grpcService.subscribeToSettledInvoices(SETTLE_INDEX_OFFSET))
                    .thenReturn(Optional.of(invoices.iterator()));
            assertThat(grpcInvoices.getNewSettledInvoicesAfter(SETTLE_INDEX_OFFSET))
                    .containsExactly(SETTLED_INVOICE, SETTLED_INVOICE_2);
        }

        @Test
        void ignores_invalid_invoice() {
            List<Invoice> invoices = List.of(
                    invoice(SETTLED, SETTLED_INVOICE_2),
                    invoice(CANCELED, null),
                    invoice(SETTLED, SETTLED_INVOICE)
            );
            when(grpcService.subscribeToSettledInvoices(SETTLE_INDEX_OFFSET))
                    .thenReturn(Optional.of(invoices.iterator()));
            assertThat(grpcInvoices.getNewSettledInvoicesAfter(SETTLE_INDEX_OFFSET))
                    .containsExactly(SETTLED_INVOICE_2, SETTLED_INVOICE);
        }

        @Test
        void empty_iterator() {
            when(grpcService.subscribeToSettledInvoices(SETTLE_INDEX_OFFSET))
                    .thenReturn(Optional.of(Collections.emptyIterator()));
            assertThat(grpcInvoices.getNewSettledInvoicesAfter(SETTLE_INDEX_OFFSET)).isEmpty();
        }

    }

    @Test
    void getLimit() {
        assertThat(grpcInvoices.getLimit()).isEqualTo(LIMIT);
    }

    @Test
    void createPaymentRequest() {
        Coins amount = Coins.ofSatoshis(123);
        String memo = "memo";

        String paymentRequest = "paymentRequest";
        when(grpcPayments.decodePaymentRequest(paymentRequest))
                .thenReturn(Optional.of(DECODED_PAYMENT_REQUEST));
        when(grpcService.addInvoice(Invoice.newBuilder()
                        .setValueMsat(amount.milliSatoshis())
                        .setExpiry(123)
                        .setMemo(memo)
                .build()))
                .thenReturn(Optional.of(AddInvoiceResponse.newBuilder().setPaymentRequest(paymentRequest).build()));

        assertThat(grpcInvoices.createPaymentRequest(amount, memo, Duration.ofSeconds(123)))
                .contains(DECODED_PAYMENT_REQUEST);
    }

    private Invoice keysendInvoice() {
        LinkedHashMap<Long, ByteString> customRecords = new LinkedHashMap<>();
        customRecords.put(5_482_373_484L, ByteString.copyFromUtf8("000"));
        customRecords.put(7_629_168L, ByteString.copyFromUtf8(KEYSEND_MESSAGE));
        return invoice(SETTLED, SETTLED_INVOICE_KEYSEND, customRecords, List.of());
    }

    private Invoice keysendInvoiceV2() {
        LinkedHashMap<Long, ByteString> customRecords = new LinkedHashMap<>();
        customRecords.put(5_482_373_484L, ByteString.copyFromUtf8("000"));
        customRecords.put(34_349_334L, ByteString.copyFromUtf8(KEYSEND_MESSAGE));
        return invoice(SETTLED, SETTLED_INVOICE_KEYSEND, customRecords, List.of());
    }

    private Invoice settledInvoiceReceivedVia(ChannelId... channelIds) {
        List<Long> receivedVia = Arrays.stream(channelIds).map(ChannelId::getShortChannelId).toList();
        return invoice(SETTLED, SETTLED_INVOICE, Map.of(), receivedVia);
    }

    private Invoice invoice(
            Invoice.InvoiceState state,
            @Nullable SettledInvoice settledInvoice
    ) {
        return invoice(state, settledInvoice, Map.of(), List.of());
    }

    private Invoice invoice(
            Invoice.InvoiceState state,
            @Nullable SettledInvoice settledInvoice,
            Map<Long, ByteString> customRecords,
            List<Long> receivedVia
    ) {
        if (settledInvoice == null) {
            return Invoice.newBuilder().setState(state).build();
        }
        List<InvoiceHTLC> htlcs = new ArrayList<>();
        if (receivedVia.isEmpty()) {
            InvoiceHTLC.Builder htlcBuilder = InvoiceHTLC.newBuilder();
            htlcBuilder.putAllCustomRecords(customRecords);
            htlcBuilder.setChanId(CHANNEL_ID.getShortChannelId());
            htlcBuilder.setAmtMsat(CHANNEL_ID.getShortChannelId());
            htlcBuilder.setState(InvoiceHTLCState.SETTLED);
            htlcs.add(htlcBuilder.build());
        } else {
            for (long channelId : receivedVia) {
                InvoiceHTLC.Builder htlcBuilder = InvoiceHTLC.newBuilder();
                htlcBuilder.putAllCustomRecords(customRecords);
                htlcBuilder.setChanId(channelId);
                htlcBuilder.setAmtMsat(channelId);
                htlcBuilder.setState(InvoiceHTLCState.SETTLED);
                htlcs.add(htlcBuilder.build());
            }
        }
        return Invoice.newBuilder()
                .setState(state)
                .setAddIndex(settledInvoice.addIndex())
                .setSettleIndex(settledInvoice.settleIndex())
                .setRHash(ByteString.copyFrom(HEX_FORMAT.parseHex(settledInvoice.hash())))
                .setMemo(settledInvoice.memo())
                .setSettleDate(settledInvoice.settleDate().toEpochSecond())
                .setAmtPaidMsat(settledInvoice.amountPaid().milliSatoshis())
                .addAllHtlcs(htlcs)
                .build();
    }
}
