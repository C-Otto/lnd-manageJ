package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.ChannelDto;
import de.cotto.lndmanagej.controller.dto.ClosedChannelDetailsDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OpenCloseStatus;
import de.cotto.lndmanagej.model.Policies;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OnChainCostService;
import de.cotto.lndmanagej.service.PolicyService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;

@RestController
@Import(ObjectMapperConfiguration.class)
@SuppressWarnings("PMD.ExcessiveImports")
@RequestMapping("/api/channel/{channelId}")
public class ChannelController {
    private final ChannelService channelService;
    private final NodeService nodeService;
    private final Metrics metrics;
    private final BalanceService balanceService;
    private final OnChainCostService onChainCostService;
    private final PolicyService policyService;
    private final FeeService feeService;

    public ChannelController(
            ChannelService channelService,
            NodeService nodeService,
            BalanceService balanceService,
            OnChainCostService onChainCostService,
            PolicyService policyService,
            FeeService feeService,
            Metrics metrics
    ) {
        this.channelService = channelService;
        this.nodeService = nodeService;
        this.balanceService = balanceService;
        this.onChainCostService = onChainCostService;
        this.policyService = policyService;
        this.feeService = feeService;
        this.metrics = metrics;
    }

    @GetMapping("/")
    public ChannelDto getBasicInformation(@PathVariable ChannelId channelId) throws NotFoundException {
        mark("getBasicInformation");
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        if (localChannel == null) {
            throw new NotFoundException();
        }
        ClosedChannelDetailsDto closeDetailsForChannel = getCloseDetailsForChannel(localChannel);
        return new ChannelDto(localChannel, closeDetailsForChannel);
    }

    @GetMapping("/details")
    public ChannelDetailsDto getDetails(@PathVariable ChannelId channelId) throws NotFoundException {
        mark("getDetails");
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        if (localChannel == null) {
            throw new NotFoundException();
        }
        Pubkey remotePubkey = localChannel.getRemotePubkey();
        String remoteAlias = nodeService.getAlias(remotePubkey);
        return new ChannelDetailsDto(
                localChannel,
                remoteAlias,
                getBalanceInformation(channelId),
                getOnChainCosts(channelId),
                getPoliciesForChannel(localChannel),
                getCloseDetailsForChannel(localChannel),
                getFeeReportDto(localChannel.getId())
        );
    }

    @GetMapping("/balance")
    public BalanceInformationDto getBalance(@PathVariable ChannelId channelId) {
        mark("getBalance");
        BalanceInformation balanceInformation = balanceService.getBalanceInformation(channelId)
                .orElse(BalanceInformation.EMPTY);
        return BalanceInformationDto.createFrom(balanceInformation);
    }

    @GetMapping("/policies")
    public PoliciesDto getPolicies(@PathVariable ChannelId channelId) {
        mark("getPolicies");
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        return getPoliciesForChannel(localChannel);
    }

    @GetMapping("/close-details")
    public ClosedChannelDetailsDto getCloseDetails(@PathVariable ChannelId channelId) throws NotFoundException {
        mark("getCloseDetails");
        ClosedChannel closedChannel = channelService.getClosedChannel(channelId).orElse(null);
        if (closedChannel == null) {
            throw new NotFoundException();
        }
        return new ClosedChannelDetailsDto(closedChannel.getCloseInitiator(), closedChannel.getCloseHeight());
    }

    @GetMapping("/fee-report")
    public FeeReportDto getFeeReport(@PathVariable ChannelId channelId) {
        mark("getFeeReport");
        return getFeeReportDto(channelId);
    }

    private FeeReportDto getFeeReportDto(ChannelId channelId) {
        return FeeReportDto.createFrom(feeService.getFeeReportForChannel(channelId));
    }

    private PoliciesDto getPoliciesForChannel(@Nullable LocalChannel channel) {
        if (channel == null || channel.getStatus().openCloseStatus() != OpenCloseStatus.OPEN) {
            return PoliciesDto.EMPTY;
        }
        Policies policies = policyService.getPolicies(channel.getId());
        return PoliciesDto.createFrom(policies);
    }

    private BalanceInformation getBalanceInformation(ChannelId channelId) {
        return balanceService.getBalanceInformation(channelId)
                .orElse(BalanceInformation.EMPTY);
    }

    private OnChainCostsDto getOnChainCosts(ChannelId channelId) {
        Coins openCosts = onChainCostService.getOpenCosts(channelId).orElse(Coins.NONE);
        Coins closeCosts = onChainCostService.getCloseCosts(channelId).orElse(Coins.NONE);
        return new OnChainCostsDto(openCosts, closeCosts);
    }

    private ClosedChannelDetailsDto getCloseDetailsForChannel(LocalChannel localChannel) {
        if (localChannel.isClosed()) {
            ClosedChannel closedChannel = localChannel.getAsClosedChannel();
            return new ClosedChannelDetailsDto(closedChannel.getCloseInitiator(), closedChannel.getCloseHeight());
        } else {
            return ClosedChannelDetailsDto.UNKNOWN;
        }
    }

    private void mark(String name) {
        metrics.mark(MetricRegistry.name(getClass(), name));
    }
}
