package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class UiDataService {

    private static final int EXPECTED_MINUTES_PER_BLOCK = 10;
    private static final int MINUTES_PER_DAY = 1_440;

    public UiDataService() {
        // default constructor
    }

    public abstract List<OpenChannelDto> getOpenChannels();

    public abstract Set<Pubkey> getPubkeys();

    public abstract ChannelDetailsDto getChannelDetails(ChannelId channelId) throws NotFoundException;

    public abstract NodeDto getNode(Pubkey pubkey);

    public abstract NodeDetailsDto getNodeDetails(Pubkey pubkey);

    public List<NodeDto> createNodeList(Collection<Pubkey> pubkeys) {
        return pubkeys.parallelStream()
                .map(this::getNode)
                .toList();
    }

    public List<NodeDto> createNodeList() {
        return createNodeList(getPubkeys());
    }

    public int calculateDaysOfBlocks(int currentBlockHeight, int pastBlockHeight) {
        int channelAgeInBlocks = currentBlockHeight - pastBlockHeight;
        return (int) Math.ceil((double) channelAgeInBlocks * EXPECTED_MINUTES_PER_BLOCK / MINUTES_PER_DAY);
    }
}
