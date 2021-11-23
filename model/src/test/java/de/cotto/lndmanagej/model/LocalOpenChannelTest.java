package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_RESERVE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_RESERVE;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class LocalOpenChannelTest {
    @Test
    void getRemotePubkey() {
        assertThat(LOCAL_OPEN_CHANNEL.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getRemotePubkey_swapped() {
        LocalOpenChannel localOpenChannel = new LocalOpenChannel(
                CHANNEL_ID,
                CHANNEL_POINT,
                CAPACITY,
                PUBKEY_2,
                PUBKEY,
                BALANCE_INFORMATION,
                LOCAL,
                false,
                true
        );
        assertThat(localOpenChannel.getRemotePubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void getLocalBalance() {
        assertThat(LOCAL_OPEN_CHANNEL.getBalanceInformation().localBalance()).isEqualTo(LOCAL_BALANCE);
    }

    @Test
    void getLocalReserve() {
        assertThat(LOCAL_OPEN_CHANNEL.getBalanceInformation().localReserve()).isEqualTo(LOCAL_RESERVE);
    }

    @Test
    void getRemoteBalance() {
        assertThat(LOCAL_OPEN_CHANNEL.getBalanceInformation().remoteBalance()).isEqualTo(REMOTE_BALANCE);
    }

    @Test
    void getRemoteReserve() {
        assertThat(LOCAL_OPEN_CHANNEL.getBalanceInformation().remoteReserve()).isEqualTo(REMOTE_RESERVE);
    }

    @Test
    void getOpenInitiator() {
        assertThat(LOCAL_OPEN_CHANNEL.getOpenInitiator()).isEqualTo(LOCAL);
    }

    @Test
    void isPrivateChannel_false() {
        assertThat(LOCAL_OPEN_CHANNEL.isPrivateChannel()).isFalse();
    }

    @Test
    void isPrivateChannel_true() {
        assertThat(LOCAL_OPEN_CHANNEL_PRIVATE.isPrivateChannel()).isTrue();
    }

    @Test
    void isActive_true() {
        assertThat(LOCAL_OPEN_CHANNEL.isActive()).isTrue();
    }

    @Test
    void isActive_false() {
        assertThat(LOCAL_OPEN_CHANNEL_2.isActive()).isFalse();
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(LocalOpenChannel.class).usingGetClass().verify();
    }
}