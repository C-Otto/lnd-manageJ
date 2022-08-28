package de.cotto.lndmanagej.payments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_3;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsTest {
    private static final long INDEX_OFFSET = 1_234;
    private static final long ALL_SETTLED_INDEX_OFFSET = 500;

    @InjectMocks
    private Payments payments;

    @Mock
    private GrpcPayments grpcPayments;

    @Mock
    private PaymentsDao dao;

    @BeforeEach
    void setUp() {
        lenient().when(dao.getIndexOffset()).thenReturn(INDEX_OFFSET);
        lenient().when(dao.getAllSettledIndexOffset()).thenReturn(ALL_SETTLED_INDEX_OFFSET);
        lenient().when(grpcPayments.getLimit()).thenReturn(1);
    }

    @Nested
    class LoadNewPayments {
        @Test
        void uses_known_offset() {
            payments.loadNewSettledPayments();
            verify(grpcPayments).getPaymentsAfter(INDEX_OFFSET);
        }

        @Test
        void saves_payment() {
            when(grpcPayments.getPaymentsAfter(INDEX_OFFSET)).thenReturn(
                    Optional.of(List.of(PAYMENT, PAYMENT_2))
            ).thenReturn(Optional.of(List.of()));
            payments.loadNewSettledPayments();
            verify(dao).save(List.of(PAYMENT, PAYMENT_2));
        }

        @Test
        void repeats_while_at_limit() {
            when(grpcPayments.getPaymentsAfter(INDEX_OFFSET))
                    .thenReturn(Optional.of(List.of(PAYMENT)))
                    .thenReturn(Optional.of(List.of(PAYMENT_2)))
                    .thenReturn(Optional.of(List.of()));
            payments.loadNewSettledPayments();
            verify(dao).save(List.of(PAYMENT));
            verify(dao).save(List.of(PAYMENT_2));
        }
    }

    @Nested
    class LoadOldSettledPayments {
        @Test
        void uses_known_offset() {
            payments.loadOldSettledPayments();
            verify(grpcPayments).getAllPaymentsAfter(ALL_SETTLED_INDEX_OFFSET);
        }

        @Test
        void does_nothing_if_indexes_are_identical() {
            when(dao.getAllSettledIndexOffset()).thenReturn(INDEX_OFFSET);
            payments.loadOldSettledPayments();
            verify(grpcPayments, never()).getAllPaymentsAfter(anyLong());
        }

        @Test
        void saves_settled_payments() {
            when(grpcPayments.getAllPaymentsAfter(ALL_SETTLED_INDEX_OFFSET)).thenReturn(
                    Optional.of(List.of(Optional.of(PAYMENT), Optional.of(PAYMENT_3)))
            ).thenReturn(Optional.of(List.of()));
            payments.loadOldSettledPayments();
            verify(dao).save(List.of(PAYMENT, PAYMENT_3));
        }

        @Test
        void advances_index_after_saving_if_all_payments_are_settled() {
            InOrder inOrder = inOrder(dao);
            when(grpcPayments.getAllPaymentsAfter(ALL_SETTLED_INDEX_OFFSET)).thenReturn(
                    Optional.of(List.of(Optional.of(PAYMENT), Optional.of(PAYMENT_2)))
            ).thenReturn(Optional.of(List.of()));
            payments.loadOldSettledPayments();
            inOrder.verify(dao).save(anyList());
            inOrder.verify(dao).setAllSettledIndexOffset(PAYMENT_2.index());
        }

        @Test
        void advances_index_after_saving_if_some_payments_are_settled() {
            InOrder inOrder = inOrder(dao);
            when(grpcPayments.getAllPaymentsAfter(ALL_SETTLED_INDEX_OFFSET)).thenReturn(
                    Optional.of(List.of(Optional.of(PAYMENT), Optional.empty(), Optional.of(PAYMENT_3)))
            ).thenReturn(Optional.of(List.of()));
            payments.loadOldSettledPayments();
            inOrder.verify(dao).save(anyList());
            inOrder.verify(dao).setAllSettledIndexOffset(PAYMENT.index());
        }

        @Test
        void ignores_non_settled_payments() {
            when(grpcPayments.getAllPaymentsAfter(ALL_SETTLED_INDEX_OFFSET)).thenReturn(
                    Optional.of(List.of(Optional.empty(), Optional.of(PAYMENT_2)))
            ).thenReturn(Optional.of(List.of()));
            payments.loadOldSettledPayments();
            verify(dao).save(List.of(PAYMENT_2));
        }

        @Test
        void repeats_while_at_limit() {
            when(grpcPayments.getAllPaymentsAfter(ALL_SETTLED_INDEX_OFFSET))
                    .thenReturn(Optional.of(List.of(Optional.of(PAYMENT))))
                    .thenReturn(Optional.of(List.of(Optional.of(PAYMENT_2))))
                    .thenReturn(Optional.of(List.of()));
            payments.loadOldSettledPayments();
            verify(dao).save(List.of(PAYMENT));
            verify(dao).save(List.of(PAYMENT_2));
        }
    }
}
