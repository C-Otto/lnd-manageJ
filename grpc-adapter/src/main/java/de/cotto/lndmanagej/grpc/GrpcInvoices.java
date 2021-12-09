package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import lnrpc.Invoice;
import lnrpc.InvoiceHTLC;
import lnrpc.ListInvoiceResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.time.ZoneOffset.UTC;

@Component
public class GrpcInvoices {
    private static final int LIMIT = 1_000;
    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final long KEYSEND_PREIMAGE = 5_482_373_484L;
    private static final long KEYSEND_DATA = 7_629_168L;
    private static final long KEYSEND_DATA_V2 = 34_349_334L;

    private final GrpcService grpcService;

    public GrpcInvoices(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public int getLimit() {
        return LIMIT;
    }

    public Optional<List<SettledInvoice>> getSettledInvoicesAfter(long offset) {
        ListInvoiceResponse list = grpcService.getInvoices(offset, LIMIT).orElse(null);
        if (list == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.getInvoicesList().stream()
                .map(this::toSettledInvoice)
                .toList());
    }

    public Stream<SettledInvoice> getNewSettledInvoicesAfter(long settleIndexOffset) {
        return grpcService.subscribeToSettledInvoices(settleIndexOffset)
                .map(this::toStream)
                .orElse(Stream.of())
                .map(this::toSettledInvoice)
                .filter(SettledInvoice::isValid);
    }

    private SettledInvoice toSettledInvoice(Invoice lndInvoice) {
        if (lndInvoice.getState() != Invoice.InvoiceState.SETTLED) {
            return SettledInvoice.INVALID;
        }
        return new SettledInvoice(
                lndInvoice.getAddIndex(),
                lndInvoice.getSettleIndex(),
                LocalDateTime.ofEpochSecond(lndInvoice.getSettleDate(), 0, UTC).atZone(UTC),
                HEX_FORMAT.formatHex(lndInvoice.getRHash().toByteArray()),
                Coins.ofMilliSatoshis(lndInvoice.getAmtPaidMsat()),
                lndInvoice.getMemo(),
                getKeysendMessage(lndInvoice),
                getLastHopChannelId(lndInvoice)
        );
    }

    private Optional<String> getKeysendMessage(Invoice lndInvoice) {
        return lndInvoice.getHtlcsList().stream()
                .map(InvoiceHTLC::getCustomRecordsMap)
                .filter(map1 -> map1.containsKey(KEYSEND_PREIMAGE))
                .filter(map -> map.containsKey(KEYSEND_DATA) || map.containsKey(KEYSEND_DATA_V2))
                .map(map -> {
                    if (map.containsKey(KEYSEND_DATA)) {
                        return map.get(KEYSEND_DATA);
                    }
                    return map.get(KEYSEND_DATA_V2);
                })
                .map(ByteString::toStringUtf8)
                .findFirst();
    }

    private Optional<ChannelId> getLastHopChannelId(Invoice lndInvoice) {
        return lndInvoice.getHtlcsList().stream()
                .map(InvoiceHTLC::getChanId)
                .filter(channelId -> channelId > 0)
                .map(ChannelId::fromShortChannelId)
                .findFirst();
    }

    private Stream<Invoice> toStream(Iterator<Invoice> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }
}
