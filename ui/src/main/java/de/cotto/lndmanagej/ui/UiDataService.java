package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public abstract class UiDataService {

    private static final int EXPECTED_MINUTES_PER_BLOCK = 10;
    private static final int MINUTES_PER_DAY = 1_440;

    public UiDataService() {
        // default constructor
    }

    public abstract NodesAndChannelsWithWarningsDto getWarnings();

    public List<OpenChannelDto> getOpenChannels() {
        return getOpenChannels(null);
    }

    public abstract List<OpenChannelDto> getOpenChannels(@Nullable String sort);

    public abstract ChannelDetailsDto getChannelDetails(ChannelId channelId) throws NotFoundException;

    public abstract NodeDto getNode(Pubkey pubkey);

    public abstract NodeDetailsDto getNodeDetails(Pubkey pubkey);

    public List<NodeDto> createNodeList(Collection<OpenChannelDto> openChannels) {
        Set<Pubkey> pubkeys = openChannels.stream()
                .map(OpenChannelDto::remotePubkey)
                .collect(toSet());
        return pubkeys.parallelStream()
                .map(this::getNode)
                .toList();
    }

    public List<NodeDto> createNodeList() {
        return createNodeList(getOpenChannels());
    }

    public int calculateDaysOfBlocks(int currentBlockHeight, int pastBlockHeight) {
        int channelAgeInBlocks = currentBlockHeight - pastBlockHeight;
        return (int) Math.ceil((double) channelAgeInBlocks * EXPECTED_MINUTES_PER_BLOCK / MINUTES_PER_DAY);
    }

    protected List<OpenChannelDto> sort(List<OpenChannelDto> input, @Nullable String sort) {
        if (sort == null) {
            return sort(input, "ratio");
        }
        Comparator<OpenChannelDto> comparator = getComparator(sort)
                .thenComparing(OpenChannelDto::channelId);
        return input.stream().sorted(comparator).toList();
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static Comparator<OpenChannelDto> getComparator(String sort) {
        return switch (sort) {
            case "announced" -> Comparator.comparing(OpenChannelDto::privateChannel);
            case "inbound" -> Comparator.comparingLong(c -> c.balanceInformation().remoteBalanceSat());
            case "ratio" -> Comparator.comparing(c -> c.balanceInformation().getOutboundPercentage());
            case "outbound" -> Comparator.comparingLong(c -> c.balanceInformation().localBalanceSat());
            case "capacity" -> Comparator.comparing(OpenChannelDto::capacitySat);
            case "localbasefee" -> Comparator.comparing(c -> Long.parseLong(c.policies().local().baseFeeMilliSat()));
            case "localfeerate" -> Comparator.comparing(c -> c.policies().local().feeRatePpm());
            case "remotebasefee" -> Comparator.comparing(c -> Long.parseLong(c.policies().remote().baseFeeMilliSat()));
            case "remotefeerate" -> Comparator.comparing(c -> c.policies().remote().feeRatePpm());
            case "alias" -> Comparator.comparing(OpenChannelDto::remoteAlias);
            default -> Comparator.comparing(OpenChannelDto::channelId);
        };
    }
}
