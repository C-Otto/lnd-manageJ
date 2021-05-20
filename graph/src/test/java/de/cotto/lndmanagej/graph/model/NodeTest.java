package de.cotto.lndmanagej.graph.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.graph.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.graph.model.NodeFixtures.NODE_WITHOUT_ALIAS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class NodeTest {

    private static final String PUBKEY = NodeFixtures.PUBKEY;
    private static final String ALIAS = NodeFixtures.ALIAS;
    private static final long LAST_UPDATE = NodeFixtures.LAST_UPDATE;

    @Test
    void builder_without_arguments() {
        assertThatNullPointerException().isThrownBy(
                () -> Node.builder().build()
        );
    }

    @Test
    void builder_without_alias_uses_pubkey() {
        Node node = Node.builder().withPubkey(PUBKEY).withLastUpdate(LAST_UPDATE).build();
        assertThat(node).isEqualTo(NODE_WITHOUT_ALIAS);
    }

    @Test
    void builder_without_pubkey() {
        assertThatNullPointerException().isThrownBy(
                () -> Node.builder().withAlias(ALIAS).withLastUpdate(LAST_UPDATE).build()
        );
    }

    @Test
    void builder_without_last_update() {
        Node node = Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).build();
        assertThat(node.getLastUpdate()).isEqualTo(0);
    }

    @Test
    void builder_with_all_arguments_pubkey_first() {
        Node node = Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).withLastUpdate(LAST_UPDATE).build();
        assertThat(node).isEqualTo(NODE);
    }

    @Test
    void builder_with_all_arguments_alias_first() {
        Node node = Node.builder().withAlias(ALIAS).withPubkey(PUBKEY).withLastUpdate(LAST_UPDATE).build();
        assertThat(node).isEqualTo(NODE);
    }

    @Test
    void testToString() {
        Node node = Node.builder().withPubkey(PUBKEY).withAlias(ALIAS).withLastUpdate(LAST_UPDATE).build();
        assertThat(node).hasToString(ALIAS);
    }

    @Test
    void testEquals_only_by_pubkey() {
        EqualsVerifier.forClass(Node.class).usingGetClass().withIgnoredFields("alias", "lastUpdate").verify();
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
        Node node1 = forPubkey("pubkey");
        Node node2 = forPubkey("pubkey");
        assertThat(node1.compareTo(node2)).isEqualTo(0);
    }

    @Test
    void compareTo_by_pubkey_smaller() {
        Node node1 = forPubkey("aaa");
        Node node2 = forPubkey("zzz");
        assertThat(node1.compareTo(node2)).isLessThan(0);
    }

    @Test
    void compareTo_by_pubkey_larger() {
        Node node1 = forPubkey("0c123");
        Node node2 = forPubkey("0b123");
        assertThat(node1.compareTo(node2)).isGreaterThan(0);
    }

    @Test
    void getAlias() {
        assertThat(NODE.getAlias()).isEqualTo(NodeFixtures.ALIAS);
    }

    @Test
    void getPubkey() {
        assertThat(NODE.getPubkey()).isEqualTo(NodeFixtures.PUBKEY);
    }

    @Test
    void getLastUpdate() {
        assertThat(NODE.getLastUpdate()).isEqualTo(NodeFixtures.LAST_UPDATE);
    }

    private Node forPubkey(String pubkey) {
        return Node.builder().withPubkey(pubkey).withAlias(ALIAS).withLastUpdate(LAST_UPDATE).build();
    }
}

