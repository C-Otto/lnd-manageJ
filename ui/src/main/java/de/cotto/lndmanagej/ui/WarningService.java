package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.ui.dto.warning.DashboardWarningDto;

import java.util.List;

public abstract class WarningService {

    public WarningService() {
        // default
    }

    public abstract List<DashboardWarningDto> getWarnings();
}
