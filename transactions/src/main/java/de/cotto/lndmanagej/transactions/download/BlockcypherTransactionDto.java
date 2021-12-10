package de.cotto.lndmanagej.transactions.download;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.lndmanagej.model.Coins;

import java.io.IOException;

@JsonDeserialize(using = BlockcypherTransactionDto.Deserializer.class)
public final class BlockcypherTransactionDto extends TransactionDto {
    public BlockcypherTransactionDto(String hash, int blockHeight, int positionInBlock, Coins fees) {
        super(hash, blockHeight, positionInBlock, fees);
    }

    public static class Deserializer extends JsonDeserializer<BlockcypherTransactionDto> {
        @Override
        public BlockcypherTransactionDto deserialize(
                JsonParser jsonParser,
                DeserializationContext context
        ) throws IOException {
            JsonNode transactionDetailsNode = jsonParser.getCodec().readTree(jsonParser);
            String hash = transactionDetailsNode.get("hash").textValue();
            int blockHeight = transactionDetailsNode.get("block_height").asInt();
            long fees = transactionDetailsNode.get("fees").asLong();
            int positionInBlock = transactionDetailsNode.get("block_index").asInt();
            return new BlockcypherTransactionDto(hash, blockHeight, positionInBlock, Coins.ofSatoshis(fees));
        }
    }
}
