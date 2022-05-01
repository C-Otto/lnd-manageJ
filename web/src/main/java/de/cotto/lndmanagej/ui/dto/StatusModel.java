package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;

public record StatusModel(boolean synced, int blockHeight, NodesAndChannelsWithWarningsDto warnings) {
}
