package de.cotto.lndmanagej;

import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChanDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.cotto.lndmanagej.MockUtil.createChannelDetails;
import static de.cotto.lndmanagej.MockUtil.createNodeDetails;
import static de.cotto.lndmanagej.MockUtil.createOpenChannels;
import static de.cotto.lndmanagej.MockUtil.getStatusModel;
@SuppressWarnings("CPD-START")
@Configuration
@EnableAutoConfiguration(
        exclude = {
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@ComponentScan("de.cotto.lndmanagej.ui")
public class DemoApplication {

    public static void main(String[] arguments) {
        SpringApplication.run(DemoApplication.class, arguments);
    }

    @Component
    public static class DataServiceMock extends UiDataService {

        @Override
        public StatusModel getStatus() {
            return getStatusModel();
        }

        @Override
        public List<OpenChannelDto> getOpenChannels() {
            return createOpenChannels();
        }

        @Override
        public ChanDetailsDto getChannelDetails(ChannelId channelId) {
            OpenChannelDto localOpenChannel = getOpenChannels().stream()
                    .filter(c -> c.channelId().equals(channelId))
                    .findFirst()
                    .orElseThrow();
            return createChannelDetails(localOpenChannel);
        }

        @Override
        public NodeDto getNode(Pubkey pubkey) {
            return getOpenChannels().stream()
                    .filter(channel -> channel.remotePubkey().equals(pubkey))
                    .map(channel -> new NodeDto(pubkey.toString(), channel.remoteAlias(), isOnline(channel)))
                    .findFirst().orElseThrow();
        }

        @Override
        public NodeDetailsDto getNodeDetails(Pubkey pubkey) {
            return createNodeDetails(getNode(pubkey));
        }

    }

    private static boolean isOnline(OpenChannelDto channel) {
        return channel.channelId().getShortChannelId() % 2 != 0;
    }
}