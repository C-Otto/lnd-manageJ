package de.cotto.lndmanagej.ui.dto;

public class StatusModelFixture {

    public static final StatusModel STATUS_MODEL_NOT_CONNECTED = new StatusModel(false, false, null);
    public static final StatusModel STATUS_MODEL_NOT_SYNCED = new StatusModel(true, false, 111_000);
    public static final StatusModel STATUS_MODEL = new StatusModel(true, true, 111_111);
}
