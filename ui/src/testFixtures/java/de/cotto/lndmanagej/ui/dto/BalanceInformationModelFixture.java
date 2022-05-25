package de.cotto.lndmanagej.ui.dto;

public class BalanceInformationModelFixture {

    public static final BalanceInformationModel BALANCE_INFORMATION_MODEL =
            new BalanceInformationModel(1_000, 100, 900, 123, 10, 113);
    public static final BalanceInformationModel BALANCE_INFORMATION_MODEL_2 =
            new BalanceInformationModel(2_000, 200, 1_800, 223, 20, 203);
    public static final BalanceInformationModel LOW_LOCAL_MODEL =
            new BalanceInformationModel(110, 10, 100, 1_010, 10, 1_000);
    public static final BalanceInformationModel LOW_REMOTE_MODEL =
            new BalanceInformationModel(1_010, 10, 1_000, 110, 10, 100);

}
