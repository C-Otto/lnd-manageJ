package de.cotto.lndmanagej.transactions.download;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.TransactionHash;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = BitapsTransactionDto.Deserializer.class)
public class BitapsTransactionDto extends TransactionDto {
    public BitapsTransactionDto(TransactionHash hash, int blockHeight, int positionInBlock, Coins fees) {
        super(hash, blockHeight, positionInBlock, fees);
    }

    static class Deserializer extends ValueDeserializer<BitapsTransactionDto> {
        @Override
        public BitapsTransactionDto deserialize(
                JsonParser jsonParser,
                DeserializationContext ctxt
        ) {
            JsonNode transactionDetailsNode = jsonParser.objectReadContext().<JsonNode>readTree(jsonParser).get("data");
            TransactionHash hash = TransactionHash.create(transactionDetailsNode.get("txId").asString());
            int blockHeight = transactionDetailsNode.get("blockHeight").asInt();
            Coins fees = Coins.ofSatoshis(transactionDetailsNode.get("fee").asLong());
            int positionInBlock = transactionDetailsNode.get("blockIndex").asInt();
            return new BitapsTransactionDto(hash, blockHeight, positionInBlock, fees);
        }
    }
}
