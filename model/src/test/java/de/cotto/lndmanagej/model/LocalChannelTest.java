package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_RESERVE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_RESERVE;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class LocalChannelTest {
    @Test
    void getRemotePubkey() {
        Channel channel = ChannelFixtures.create(PUBKEY_2, PUBKEY, CHANNEL_ID);
        LocalChannel localChannel = new LocalChannel(channel, PUBKEY, BALANCE_INFORMATION);
        assertThat(localChannel.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getRemotePubkey_swapped() {
        Channel channel = ChannelFixtures.create(PUBKEY_3, PUBKEY_2, CHANNEL_ID);
        LocalChannel localChannel = new LocalChannel(channel, PUBKEY_3, BALANCE_INFORMATION);
        assertThat(localChannel.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void ownPubkey_not_in_pubkey_set() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LocalChannel(CHANNEL_2, PUBKEY_3, BALANCE_INFORMATION))
                .withMessage("Channel must have given pubkey as peer");
    }

    @Test
    void getLocalBalance() {
        assertThat(LOCAL_CHANNEL.getBalanceInformation().localBalance()).isEqualTo(LOCAL_BALANCE);
    }

    @Test
    void getLocalReserve() {
        assertThat(LOCAL_CHANNEL.getBalanceInformation().localReserve()).isEqualTo(LOCAL_RESERVE);
    }

    @Test
    void getRemoteBalance() {
        assertThat(LOCAL_CHANNEL.getBalanceInformation().remoteBalance()).isEqualTo(REMOTE_BALANCE);
    }

    @Test
    void getRemoteReserve() {
        assertThat(LOCAL_CHANNEL.getBalanceInformation().remoteReserve()).isEqualTo(REMOTE_RESERVE);
    }
}