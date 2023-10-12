package de.cotto.lndmanagej.grpc;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executor;

public class MacaroonCallCredential extends CallCredentials {
    private final String macaroon;

    MacaroonCallCredential(File macaroonFile) throws IOException {
        super();
        this.macaroon = Hex.encodeHexString(Files.readAllBytes(macaroonFile.toPath()));
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier metadataApplier) {
        appExecutor.execute(() -> {
            Metadata headers = new Metadata();
            Metadata.Key<String> macaroonKey = Metadata.Key.of("macaroon", Metadata.ASCII_STRING_MARSHALLER);
            headers.put(macaroonKey, macaroon);
            metadataApplier.apply(headers);
        });
    }
}
