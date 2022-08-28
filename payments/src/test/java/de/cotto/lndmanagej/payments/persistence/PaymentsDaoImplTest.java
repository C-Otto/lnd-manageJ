package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.Payment;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_INDEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentsDaoImplTest {
    @InjectMocks
    private PaymentsDaoImpl dao;

    @Mock
    private PaymentsRepository repository;

    @Mock
    private SettledPaymentIndexRepository settledPaymentIndexRepository;

    @Nested
    class Payments {
        @Test
        void getMaxIndex_initially_0() {
            when(repository.getMaxIndex()).thenReturn(0L);
            assertThat(dao.getIndexOffset()).isEqualTo(0L);
        }

        @Test
        void getMaxIndex() {
            when(repository.getMaxIndex()).thenReturn(123L);
            assertThat(dao.getIndexOffset()).isEqualTo(123L);
        }

        @Test
        void save_single() {
            dao.save(PAYMENT);
            verify(repository).save(argThat(jpaDto -> jpaDto.getPaymentIndex() == PAYMENT_INDEX));
        }

        @Test
        void save_empty() {
            dao.save(Set.of());
            verify(repository).saveAll(List.of());
        }

        @Test
        void save_two() {
            dao.save(Set.of(PAYMENT, PAYMENT_2));
            Set<Payment> expected = Set.of(PAYMENT, PAYMENT_2);
            verify(repository).saveAll(argThat(isSet(expected)));
        }

        @SuppressWarnings("PMD.LinguisticNaming")
        private <S extends PaymentJpaDto> ArgumentMatcher<Iterable<S>> isSet(Set<Payment> expected) {
            return iterable -> iterable instanceof List && ((List<S>) iterable).stream()
                    .map(PaymentJpaDto::toModel)
                    .collect(Collectors.toSet())
                    .equals(expected);
        }
    }

    @Nested
    class SettledPaymentIndex {
        @Test
        void initially_zero() {
            when(settledPaymentIndexRepository.findByEntityId(0)).thenReturn(Optional.empty());
            assertThat(dao.getAllSettledIndexOffset()).isEqualTo(0);
        }

        @Test
        void has_value() {
            SettledPaymentIndexJpaDto entity = new SettledPaymentIndexJpaDto();
            entity.setAllSettledIndexOffset(23);
            when(settledPaymentIndexRepository.findByEntityId(0)).thenReturn(Optional.of(entity));
            assertThat(dao.getAllSettledIndexOffset()).isEqualTo(23);
        }

        @Test
        void can_be_set() {
            dao.setAllSettledIndexOffset(42);
            verifySave(42);
        }

        @Test
        void can_be_updated() {
            SettledPaymentIndexJpaDto entity = new SettledPaymentIndexJpaDto();
            entity.setAllSettledIndexOffset(23);
            lenient().when(settledPaymentIndexRepository.findByEntityId(0)).thenReturn(Optional.of(entity));
            dao.setAllSettledIndexOffset(123);
            verifySave(123);
        }

        @Test
        void uses_existing_instance_for_update() {
            SettledPaymentIndexJpaDto existingInstance = new SettledPaymentIndexJpaDto();
            existingInstance.setAllSettledIndexOffset(23);
            when(settledPaymentIndexRepository.findByEntityId(0)).thenReturn(Optional.of(existingInstance));
            dao.setAllSettledIndexOffset(123);
            assertThat(existingInstance.getAllSettledIndexOffset()).isEqualTo(123);
            verify(settledPaymentIndexRepository).save(argThat(arg -> arg == existingInstance));
            verifySave(123);
        }

        private void verifySave(int offset) {
            verify(settledPaymentIndexRepository).save(argThat(arg -> arg.getAllSettledIndexOffset() == offset));
            verify(settledPaymentIndexRepository).save(argThat(arg -> arg.getEntityId() == 0));
        }
    }
}
