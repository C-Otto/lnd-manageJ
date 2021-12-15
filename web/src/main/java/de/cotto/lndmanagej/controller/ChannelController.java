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
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OpenCloseStatus;
import de.cotto.lndmanagej.model.Policies;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OffChainCostService;
import de.cotto.lndmanagej.service.OnChainCostService;
import de.cotto.lndmanagej.service.PolicyService;
import de.cotto.lndmanagej.service.RebalanceService;
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
    private final BalanceService balanceService;
    private final OnChainCostService onChainCostService;
    private final PolicyService policyService;
    private final FeeService feeService;
    private final OffChainCostService offChainCostService;
    private final RebalanceService rebalanceService;

    public ChannelController(
            ChannelService channelService,
            NodeService nodeService,
            BalanceService balanceService,
            OnChainCostService onChainCostService,
            PolicyService policyService,
            FeeService feeService,
            OffChainCostService offChainCostService,
            RebalanceService rebalanceService
    ) {
        this.channelService = channelService;
        this.nodeService = nodeService;
        this.balanceService = balanceService;
        this.onChainCostService = onChainCostService;
        this.policyService = policyService;
        this.feeService = feeService;
        this.offChainCostService = offChainCostService;
        this.rebalanceService = rebalanceService;
    }

    @Timed
    @GetMapping("/")
    public ChannelDto getBasicInformation(@PathVariable ChannelId channelId) throws NotFoundException {
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElse(null);
        if (localChannel == null) {
            throw new NotFoundException();
        }
        ClosedChannelDetailsDto closeDetailsForChannel = getCloseDetailsForChannel(localChannel);
        return new ChannelDto(localChannel, closeDetailsForChannel);
    }

    @Timed
    @GetMapping("/details")
    public ChannelDetailsDto getDetails(@PathVariable ChannelId channelId) throws NotFoundException {
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
                onChainCostService.getOnChainCostsForChannelId(channelId),
                offChainCostService.getOffChainCostsForChannel(channelId),
                getPoliciesForChannel(localChannel),
                getCloseDetailsForChannel(localChannel),
                getFeeReportFromService(localChannel.getId()),
                rebalanceService.getRebalanceAmountFromChannel(localChannel.getId()),
                rebalanceService.getRebalanceAmountToChannel(localChannel.getId())
        );
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
        return getPoliciesForChannel(localChannel);
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
        return FeeReportDto.createFromModel(getFeeReportFromService(channelId));
    }

    private FeeReport getFeeReportFromService(ChannelId channelId) {
        return feeService.getFeeReportForChannel(channelId);
    }

    private PoliciesDto getPoliciesForChannel(@Nullable LocalChannel channel) {
        if (channel == null || channel.getStatus().openCloseStatus() != OpenCloseStatus.OPEN) {
            return PoliciesDto.EMPTY;
        }
        Policies policies = policyService.getPolicies(channel.getId());
        return PoliciesDto.createFromModel(policies);
    }

    private BalanceInformation getBalanceInformation(ChannelId channelId) {
        return balanceService.getBalanceInformation(channelId)
                .orElse(BalanceInformation.EMPTY);
    }

    private ClosedChannelDetailsDto getCloseDetailsForChannel(LocalChannel localChannel) {
        return ClosedChannelDetailsDto.createFromModel(localChannel);
    }
}
