package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Policies;

public record PoliciesDto(
        PolicyDto local,
        PolicyDto remote
) {
    public static PoliciesDto createFromModel(Policies policies) {
        return new PoliciesDto(
                PolicyDto.createFromModel(policies.local()),
                PolicyDto.createFromModel(policies.remote())
        );
    }
}
