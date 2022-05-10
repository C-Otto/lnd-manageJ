package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class UiDataService {

    public UiDataService() {
        // default constructor
    }

    public abstract StatusModel getStatus();

    public abstract List<OpenChannelDto> getOpenChannels();

    public abstract ChannelDetailsDto getChannelDetails(ChannelId channelId) throws NotFoundException;

    public abstract NodeDto getNode(Pubkey pubkey);

    public abstract NodeDetailsDto getNodeDetails(Pubkey pubkey);

    public List<NodeDto> createNodeList(Collection<OpenChannelDto> openChannels) {
        Set<Pubkey> pubkeys = openChannels.stream()
                .map(OpenChannelDto::remotePubkey)
                .collect(Collectors.toSet());
        return pubkeys.stream()
                .map(this::getNode)
                .collect(Collectors.toList());
    }

    public List<NodeDto> createNodeList() {
        return createNodeList(this.getOpenChannels());
    }
}
