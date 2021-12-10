package de.cotto.lndmanagej.transactions.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;

class BitapsTransactionDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                  "data": {
                    "txId": "%s",
                    "blockIndex": %d,
                    "fee": %d,
                    "blockHeight": %d
                  }
                }
                """.formatted(
                TRANSACTION_HASH, POSITION_IN_BLOCK, FEES.satoshis(), BLOCK_HEIGHT
        );
        BitapsTransactionDto bitapsTransactionDto =
                objectMapper.readValue(json, BitapsTransactionDto.class);
        assertThat(bitapsTransactionDto.toModel()).isEqualTo(TRANSACTION);
    }
}