package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.NodeWarning;

import java.util.stream.Stream;

public interface NodeWarningsProvider {
    Stream<NodeWarning> getNodeWarnings(Pubkey pubkey);
}
