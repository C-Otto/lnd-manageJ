package de.cotto.lndmanagej.transactions.download;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.lndmanagej.model.Coins;

import java.io.IOException;

@JsonDeserialize(using = BitapsTransactionDto.Deserializer.class)
public class BitapsTransactionDto extends TransactionDto {
    public BitapsTransactionDto(String hash, int blockHeight, int positionInBlock, Coins fees) {
        super(hash, blockHeight, positionInBlock, fees);
    }

    static class Deserializer extends JsonDeserializer<BitapsTransactionDto> {
        @Override
        public BitapsTransactionDto deserialize(
                JsonParser jsonParser,
                DeserializationContext deserializationContext
        ) throws IOException {
            JsonNode transactionDetailsNode = jsonParser.getCodec().<JsonNode>readTree(jsonParser).get("data");
            String hash = transactionDetailsNode.get("txId").textValue();
            int blockHeight = transactionDetailsNode.get("blockHeight").asInt();
            long fees = transactionDetailsNode.get("fee").asLong();
            int positionInBlock = transactionDetailsNode.get("blockIndex").asInt();
            return new BitapsTransactionDto(hash, blockHeight, positionInBlock, Coins.ofSatoshis(fees));
        }
    }
}
