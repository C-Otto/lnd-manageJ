package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Policies;

public record PoliciesDto(
        PolicyDto local,
        PolicyDto remote
) {
    public static final PoliciesDto EMPTY =
            new PoliciesDto(PolicyDto.EMPTY, PolicyDto.EMPTY);

    public static PoliciesDto createFrom(Policies policies) {
        return new PoliciesDto(
                PolicyDto.createFrom(policies.local()),
                PolicyDto.createFrom(policies.remote())
        );
    }
}
