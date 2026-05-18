package de.cotto.lndmanagej.transactions.download;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.TransactionHash;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = BlockcypherTransactionDto.Deserializer.class)
public final class BlockcypherTransactionDto extends TransactionDto {
    public BlockcypherTransactionDto(TransactionHash hash, int blockHeight, int positionInBlock, Coins fees) {
        super(hash, blockHeight, positionInBlock, fees);
    }

    public static class Deserializer extends
            ValueDeserializer<BlockcypherTransactionDto> {
        @Override
        public BlockcypherTransactionDto deserialize(
                JsonParser jsonParser,
                DeserializationContext context
        ) {
            JsonNode transactionDetailsNode = jsonParser.objectReadContext().readTree(jsonParser);
            TransactionHash hash = TransactionHash.create(transactionDetailsNode.get("hash").stringValue());
            int blockHeight = transactionDetailsNode.get("block_height").asInt();
            Coins fees = Coins.ofSatoshis(transactionDetailsNode.get("fees").asLong());
            int positionInBlock = transactionDetailsNode.get("block_index").asInt();
            return new BlockcypherTransactionDto(hash, blockHeight, positionInBlock, fees);
        }
    }
}
