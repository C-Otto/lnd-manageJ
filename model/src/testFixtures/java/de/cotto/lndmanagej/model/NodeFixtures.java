package de.cotto.lndmanagej.model;

import java.time.Instant;

public class NodeFixtures {
    public static final String PUBKEY = "027abc123abc123abc123abc123123abc123abc123abc123abc123abc123abc123";
    public static final String PUBKEY_2 = "03fff0000000000000000000000000000000000000000000000000000000000000";
    public static final String ALIAS = "Node";
    public static final String ALIAS_2 = "Another Node";
    public static final long LAST_UPDATE = Instant.now().toEpochMilli() / 1_000;

    public static final Node NODE = Node.builder()
            .withPubkey(PUBKEY)
            .withAlias(ALIAS)
            .withLastUpdate(LAST_UPDATE)
            .build();

    public static final Node NODE_2 = Node.builder()
            .withPubkey(PUBKEY_2)
            .withAlias(ALIAS_2)
            .withLastUpdate(LAST_UPDATE)
            .build();

    public static final Node NODE_WITHOUT_ALIAS = Node.builder()
            .withPubkey(PUBKEY)
            .withLastUpdate(LAST_UPDATE)
            .build();
}
