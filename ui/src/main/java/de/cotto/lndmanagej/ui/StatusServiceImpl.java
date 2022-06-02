package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.service.OwnNodeService;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class StatusServiceImpl implements StatusService {

    private final OwnNodeService ownNodeService;

    public StatusServiceImpl(OwnNodeService ownNodeService) {
        this.ownNodeService = ownNodeService;
    }

    @Override
    public StatusModel getStatus() {

        boolean connected = true;
        boolean synced = ownNodeService.isSyncedToChain();
        Integer blockHeight = null;
        try {
            blockHeight = ownNodeService.getBlockHeight();
        } catch (NoSuchElementException e) {
            connected = false;
        }

        return new StatusModel(connected, synced, blockHeight);
    }

}
