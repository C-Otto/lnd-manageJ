package de.cotto.lndmanagej.model;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Objects;

public final class ChannelPoint {
    private static final Splitter SPLITTER = Splitter.on(":");

    private final String transactionHash;
    private final int index;

    private ChannelPoint(String transactionHash, Integer index) {
        this.transactionHash = transactionHash;
        this.index = index;
    }

    public static ChannelPoint create(String channelPoint) {
        List<String> split = SPLITTER.splitToList(channelPoint);
        return new ChannelPoint(split.get(0), Integer.valueOf(split.get(1)));
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public int getIndex() {
        return index;
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
        return index == that.index && Objects.equals(transactionHash, that.transactionHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionHash, index);
    }

    @Override
    public String toString() {
        return transactionHash + ':' + index;
    }
}
