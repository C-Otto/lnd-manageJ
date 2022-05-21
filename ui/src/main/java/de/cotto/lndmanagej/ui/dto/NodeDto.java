package de.cotto.lndmanagej.ui.dto;

import java.io.Serializable;
import java.util.Comparator;

public record NodeDto(String pubkey, String alias, boolean online) {

    public static class OnlineStatusAndAliasComparator implements Comparator<NodeDto>, Serializable {

        @Override
        public int compare(NodeDto node1, NodeDto node2) {
            boolean bothOffline = !node1.online && !node2.online;
            boolean bothOnline = node1.online && node2.online;
            if (bothOffline || bothOnline) {
                return node1.alias.compareToIgnoreCase(node2.alias);
            }
            return node1.online ? 1 : -1;
        }
    }
}
