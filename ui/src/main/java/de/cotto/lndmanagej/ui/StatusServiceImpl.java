package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.service.OwnNodeService;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import org.springframework.stereotype.Component;

@Component
public class StatusServiceImpl implements StatusService {

    private final OwnNodeService ownNodeService;

    public StatusServiceImpl(OwnNodeService ownNodeService) {
        this.ownNodeService = ownNodeService;
    }

    @Override
    public StatusModel getStatus() {
        return new StatusModel(ownNodeService.isSyncedToChain(), ownNodeService.getBlockHeight());
    }
}
