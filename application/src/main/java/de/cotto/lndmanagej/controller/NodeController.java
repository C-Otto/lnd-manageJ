package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/node/")
public class NodeController {
    private final GrpcNodeInfo grpcNodeInfo;

    public NodeController(GrpcNodeInfo grpcNodeInfo) {
        this.grpcNodeInfo = grpcNodeInfo;
    }

    @GetMapping("/{pubkey}/alias")
    public String getAlias(@PathVariable Pubkey pubkey) {
        return grpcNodeInfo.getNode(pubkey).alias();
    }
}
