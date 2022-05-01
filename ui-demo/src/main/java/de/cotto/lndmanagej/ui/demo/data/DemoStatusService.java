package de.cotto.lndmanagej.ui.demo.data;

import de.cotto.lndmanagej.ui.StatusService;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import org.springframework.stereotype.Component;

@Component
public class DemoStatusService implements StatusService {

    public DemoStatusService() {
        super();
    }

    @Override
    public StatusModel getStatus() {
        return new StatusModel(true, 735_895);
    }
}


