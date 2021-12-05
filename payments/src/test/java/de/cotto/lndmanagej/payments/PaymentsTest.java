package de.cotto.lndmanagej.payments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsTest {
    private static final long INDEX_OFFSET = 1_234;

    @InjectMocks
    private Payments payments;

    @Mock
    private GrpcPayments grpcPayments;

    @Mock
    private PaymentsDao dao;

    @BeforeEach
    void setUp() {
        when(dao.getIndexOffset()).thenReturn(INDEX_OFFSET);
        when(grpcPayments.getLimit()).thenReturn(1);
    }

    @Test
    void refresh_uses_known_offset() {
        payments.refresh();
        verify(grpcPayments).getPaymentsAfter(INDEX_OFFSET);
    }

    @Test
    void refresh_saves_payment() {
        when(grpcPayments.getPaymentsAfter(INDEX_OFFSET)).thenReturn(
                Optional.of(List.of(PAYMENT, PAYMENT_2))
        ).thenReturn(Optional.of(List.of()));
        payments.refresh();
        verify(dao).save(List.of(PAYMENT, PAYMENT_2));
    }

    @Test
    void refresh_repeats_while_at_limit() {
        when(grpcPayments.getPaymentsAfter(INDEX_OFFSET))
                .thenReturn(Optional.of(List.of(PAYMENT)))
                .thenReturn(Optional.of(List.of(PAYMENT_2)))
                .thenReturn(Optional.of(List.of()));
        payments.refresh();
        verify(dao).save(List.of(PAYMENT));
        verify(dao).save(List.of(PAYMENT_2));
    }
}