package de.cotto.lndmanagej.transactions.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.transactions.model.Transaction;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Entity
@Table(name = "transactions")
class TransactionJpaDto {
    @Id
    @Nullable
    private String hash;

    private int blockHeight;

    private long fees;

    private int positionInBlock;

    protected TransactionJpaDto() {
        // for JPA
    }

    protected static TransactionJpaDto fromModel(Transaction transaction) {
        TransactionJpaDto dto = new TransactionJpaDto();
        dto.setHash(transaction.hash().getHash());
        dto.setBlockHeight(transaction.blockHeight());
        dto.setFees(transaction.fees().satoshis());
        dto.setPositionInBlock(transaction.positionInBlock());
        return dto;
    }

    protected Optional<Transaction> toModel() {
        if (hash == null) {
            return Optional.empty();
        }
        return Optional.of(new Transaction(
                TransactionHash.create(hash),
                blockHeight,
                positionInBlock,
                Coins.ofSatoshis(fees)
        ));
    }

    @VisibleForTesting
    protected void setHash(@Nonnull String hash) {
        this.hash = hash;
    }

    @VisibleForTesting
    protected void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    @VisibleForTesting
    protected void setFees(long fees) {
        this.fees = fees;
    }

    @VisibleForTesting
    protected void setPositionInBlock(int positionInBlock) {
        this.positionInBlock = positionInBlock;
    }

    @VisibleForTesting
    protected int getBlockHeight() {
        return blockHeight;
    }

    @CheckForNull
    @VisibleForTesting
    protected String getHash() {
        return hash;
    }

    @VisibleForTesting
    protected int getPositionInBlock() {
        return positionInBlock;
    }

    @VisibleForTesting
    protected long getFees() {
        return fees;
    }
}
