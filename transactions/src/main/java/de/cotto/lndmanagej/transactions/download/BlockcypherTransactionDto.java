package de.cotto.lndmanagej.transactions.download;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.TransactionHash;

import java.io.IOException;

@JsonDeserialize(using = BlockcypherTransactionDto.Deserializer.class)
public final class BlockcypherTransactionDto extends TransactionDto {
    public BlockcypherTransactionDto(TransactionHash hash, int blockHeight, int positionInBlock, Coins fees) {
        super(hash, blockHeight, positionInBlock, fees);
    }

    public static class Deserializer extends JsonDeserializer<BlockcypherTransactionDto> {
        @Override
        public BlockcypherTransactionDto deserialize(
                JsonParser jsonParser,
                DeserializationContext context
        ) throws IOException {
            JsonNode transactionDetailsNode = jsonParser.getCodec().readTree(jsonParser);
            TransactionHash hash = TransactionHash.create(transactionDetailsNode.get("hash").textValue());
            int blockHeight = transactionDetailsNode.get("block_height").asInt();
            Coins fees2 = Coins.ofSatoshis(transactionDetailsNode.get("fees").asLong());
            int positionInBlock = transactionDetailsNode.get("block_index").asInt();
            return new BlockcypherTransactionDto(hash, blockHeight, positionInBlock, fees2);
        }
    }
}
