package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.LAST_UPDATE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_WITHOUT_ALIAS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class NodeTest {
    @Test
    void forPubkey() {
        assertThat(Node.forPubkey(PUBKEY)).isEqualTo(Node.builder().withPubkey(PUBKEY).build());
    }

    @Test
    void builder_without_arguments() {
        assertThatNullPointerException().isThrownBy(
                () -> Node.builder().build()
        );
    }

    @Test
    void builder_without_alias_uses_pubkey() {
        Node node = Node.builder().withPubkey(PUBKEY).withLastUpdate(LAST_UPDATE).withOnlineStatus(false).build();
        assertThat(node).isEqualTo(NODE_WITHOUT_ALIAS);
    }

    @Test
    void builder_without_pubkey() {
        assertThatNullPointerException().isThrownBy(
                () -> Node.builder().withAlias(ALIAS).withLastUpdate(LAST_UPDATE).withOnlineStatus(false).build()
        );
    }

    @Test
    void builder_without_last_update() {
        Node node = Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).withOnlineStatus(false).build();
        assertThat(node.lastUpdate()).isEqualTo(0);
    }

    @Test
    void builder_without_online_status() {
        Node node = Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).withLastUpdate(LAST_UPDATE).build();
        assertThat(node).isEqualTo(NODE);
    }

    @Test
    void builder_with_all_arguments_pubkey_first() {
        Node node = Node.builder()
                .withPubkey(PUBKEY)
                .withAlias(ALIAS)
                .withOnlineStatus(false)
                .withLastUpdate(LAST_UPDATE)
                .build();
        assertThat(node).isEqualTo(NODE);
    }

    @Test
    void builder_with_all_arguments_alias_first() {
        Node node = Node.builder()
                .withAlias(ALIAS)
                .withPubkey(PUBKEY)
                .withLastUpdate(LAST_UPDATE)
                .withOnlineStatus(true)
                .build();
        assertThat(node).isEqualTo(NODE_PEER);
    }

    @Test
    void testToString() {
        Node node = Node.builder()
                .withPubkey(PUBKEY)
                .withAlias(ALIAS)
                .withLastUpdate(LAST_UPDATE)
                .withOnlineStatus(false)
                .build();
        assertThat(node).hasToString(ALIAS);
    }

    @Test
    void testEquals_only_by_pubkey() {
        EqualsVerifier.forClass(Node.class).usingGetClass().withIgnoredFields("alias", "lastUpdate", "online").verify();
    }

    @Test
    void testEquals_different_aliases() {
        Node node1 = Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).withLastUpdate(LAST_UPDATE).build();
        Node node2 = Node.builder().withPubkey(PUBKEY).withAlias("alias2").withLastUpdate(LAST_UPDATE).build();
        assertThat(node1).isEqualTo(node2);
    }

    @Test
    void testEquals_different_lastUpdate() {
        Node node1 = Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).withLastUpdate(LAST_UPDATE).build();
        Node node2 = Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).withLastUpdate(LAST_UPDATE + 1).build();
        assertThat(node1).isEqualTo(node2);
    }

    @Test
    void compareTo_by_pubkey_same() {
        Node node1 = createFor("aaa000aaa000abc000abc000abc000abc000abc000abc000abc000abc000abc000");
        Node node2 = createFor("aaa000aaa000abc000abc000abc000abc000abc000abc000abc000abc000abc000");
        assertThat(node1.compareTo(node2)).isEqualTo(0);
    }

    @Test
    void compareTo_by_pubkey_smaller() {
        Node node1 = createFor("aaa00abc000abc000abc000abc000abc000abc000abc000abc000abc000abc000a");
        Node node2 = createFor("fff00abc000abc000abc000abc000abc000abc000abc000abc000abc000abc000a");
        assertThat(node1.compareTo(node2)).isLessThan(0);
    }

    @Test
    void compareTo_by_pubkey_larger() {
        Node node1 = createFor("0c123abc000abc000abc000abc000abc000abc000abc000abc000abc000abc000a");
        Node node2 = createFor("0b123abc000abc000abc000abc000abc000abc000abc000abc000abc000abc000a");
        assertThat(node1.compareTo(node2)).isGreaterThan(0);
    }

    @Test
    void getAlias() {
        assertThat(NODE.alias()).isEqualTo(ALIAS);
    }

    @Test
    void getPubkey() {
        assertThat(NODE.pubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void getLastUpdate() {
        assertThat(NODE.lastUpdate()).isEqualTo(LAST_UPDATE);
    }

    @Test
    void online() {
        assertThat(NODE.online()).isFalse();
        assertThat(NODE_PEER.online()).isTrue();
    }

    private Node createFor(String pubkey) {
        return Node.forPubkey(Pubkey.create(pubkey));
    }
}

