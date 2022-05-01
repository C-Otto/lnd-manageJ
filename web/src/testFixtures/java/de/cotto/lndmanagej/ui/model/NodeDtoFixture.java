package de.cotto.lndmanagej.ui.model;

import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

public class NodeDtoFixture {

    public static NodeDto createFrom(NodeDetailsDto details) {
        return new NodeDto(details.node().toString(), details.alias(), details.onlineReport().online());
    }

    public static NodeDto createFrom(OpenChannelDto channel) {
        return new NodeDto(channel.remotePubkey().toString(), channel.remoteAlias(), true);
    }
}
