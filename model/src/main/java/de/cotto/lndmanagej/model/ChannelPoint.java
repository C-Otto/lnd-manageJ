package de.cotto.lndmanagej.model;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Objects;

public final class ChannelPoint {
    private static final Splitter SPLITTER = Splitter.on(":");

    private final TransactionHash transactionHash;
    private final int output;

    private ChannelPoint(TransactionHash transactionHash, Integer output) {
        this.transactionHash = transactionHash;
        this.output = output;
    }

    public static ChannelPoint create(String channelPoint) {
        List<String> split = SPLITTER.splitToList(channelPoint);
        return new ChannelPoint(TransactionHash.create(split.get(0)), Integer.valueOf(split.get(1)));
    }

    public TransactionHash getTransactionHash() {
        return transactionHash;
    }

    public int getOutput() {
        return output;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ChannelPoint that = (ChannelPoint) other;
        return output == that.output && Objects.equals(transactionHash, that.transactionHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionHash, output);
    }

    @Override
    public String toString() {
        return transactionHash.toString() + ':' + output;
    }
}
