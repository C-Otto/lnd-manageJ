package de.cotto.lndmanagej.model;

import java.time.Instant;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;

public class NodeFixtures {
    public static final String ALIAS = "Node";
    public static final String ALIAS_2 = "Another Node";
    public static final int LAST_UPDATE = (int) Instant.now().getEpochSecond();

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
