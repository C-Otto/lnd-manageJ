package de.cotto.lndmanagej.transactions.download;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;

class BlockcypherTransactionDtoTest {
    private final ObjectMapper objectMapper = new TestObjectMapper();

    @Test
    void deserialization() throws Exception {
        String json = """
                {
                  "block_height": %d,
                  "hash": "%s",
                  "fees": %d,
                  "confirmed": "2019-10-26T20:06:35Z",
                  "received": "2019-10-26T20:06:34Z",
                  "block_index": %d,
                  "inputs": [
                    {
                      "output_value": 100,
                      "addresses": [
                        "aaa"
                      ]
                    },
                    {
                      "output_value": 200,
                      "addresses": [
                        "bbb"
                      ]
                    }
                  ],
                  "outputs": [
                    {
                      "value": 123,
                      "addresses": [
                        "abc"
                      ]
                    },
                    {
                      "value": 456,
                      "addresses": [
                        "def"
                      ]
                    }
                  ]
                }""".formatted(
                BLOCK_HEIGHT, TRANSACTION_HASH, FEES.satoshis(), POSITION_IN_BLOCK
        );
        BlockcypherTransactionDto blockcypherTransactionDto =
                objectMapper.readValue(json, BlockcypherTransactionDto.class);
        assertThat(blockcypherTransactionDto.toModel()).isEqualTo(TRANSACTION);
    }
}