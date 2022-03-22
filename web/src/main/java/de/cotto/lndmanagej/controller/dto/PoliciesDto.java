package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.PoliciesForLocalChannel;

public record PoliciesDto(
        PolicyDto local,
        PolicyDto remote
) {
    public static PoliciesDto createFromModel(PoliciesForLocalChannel policiesForLocalChannel) {
        return new PoliciesDto(
                PolicyDto.createFromModel(policiesForLocalChannel.local()),
                PolicyDto.createFromModel(policiesForLocalChannel.remote())
        );
    }
}
