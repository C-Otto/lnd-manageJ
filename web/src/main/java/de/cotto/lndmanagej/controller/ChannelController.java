package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.ChannelDto;
import de.cotto.lndmanagej.controller.dto.ClosedChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OpenCloseStatus;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelDetailsService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.PolicyService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Import(ObjectMapperConfiguration.class)
@SuppressWarnings("PMD.ExcessiveImports")
@RequestMapping("/api/channel/{channelId}")
public class ChannelController {
    private final ChannelService channelService;
    private final BalanceService balanceService;
    private final PolicyService policyService;
    private final FeeService feeService;
    private final ChannelDetailsService channelDetailsService;

    public ChannelController(
            ChannelService channelService,
            BalanceService balanceService,
            PolicyService policyService,
            FeeService feeService,
            ChannelDetailsService channelDetailsService
    ) {
        this.channelService = channelService;
        this.balanceService = balanceService;
        this.policyService = policyService;
        this.feeService = feeService;
        this.channelDetailsService = channelDetailsService;
    }

    @Timed
    @GetMapping("/")
    public ChannelDto getBasicInformation(@PathVariable ChannelId channelId) throws NotFoundException {
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        if (localChannel == null) {
            throw new NotFoundException();
        }
        return new ChannelDto(localChannel);
    }

    @Timed
    @GetMapping("/details")
    public ChannelDetailsDto getDetails(@PathVariable ChannelId channelId) throws NotFoundException {
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        if (localChannel == null) {
            throw new NotFoundException();
        }
        return ChannelDetailsDto.createFromModel(channelDetailsService.getDetails(localChannel));
    }

    @Timed
    @GetMapping("/balance")
    public BalanceInformationDto getBalance(@PathVariable ChannelId channelId) {
        BalanceInformation balanceInformation = balanceService.getBalanceInformation(channelId)
                .orElse(BalanceInformation.EMPTY);
        return BalanceInformationDto.createFromModel(balanceInformation);
    }

    @Timed
    @GetMapping("/policies")
    public PoliciesDto getPolicies(@PathVariable ChannelId channelId) {
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        if (localChannel == null || localChannel.getStatus().openCloseStatus() != OpenCloseStatus.OPEN) {
            return PoliciesDto.createFromModel(PoliciesForLocalChannel.UNKNOWN);
        }
        return PoliciesDto.createFromModel(policyService.getPolicies(localChannel));
    }

    @Timed
    @GetMapping("/close-details")
    public ClosedChannelDetailsDto getCloseDetails(@PathVariable ChannelId channelId) throws NotFoundException {
        ClosedChannel closedChannel = channelService.getClosedChannel(channelId).orElse(null);
        if (closedChannel == null) {
            throw new NotFoundException();
        }
        return ClosedChannelDetailsDto.createFromModel(closedChannel);
    }

    @Timed
    @GetMapping("/fee-report")
    public FeeReportDto getFeeReport(@PathVariable ChannelId channelId) {
        return FeeReportDto.createFromModel(feeService.getFeeReportForChannel(channelId));
    }

}
