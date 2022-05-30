package de.cotto.lndmanagej.ui.dto;

import javax.annotation.Nullable;

public record StatusModel(boolean connected, boolean synced, @Nullable Integer blockHeight) {
}