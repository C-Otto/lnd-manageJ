package de.cotto.lndmanagej;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class LndConfigurationTest {

    private final LndConfiguration lndConfiguration = new LndConfiguration();

    @Test
    void host() {
        String host = "host";
        lndConfiguration.setHost(host);
        assertThat(lndConfiguration.getHost()).isEqualTo(host);
    }

    @Test
    void port() {
        int port = 123;
        lndConfiguration.setPort(port);
        assertThat(lndConfiguration.getPort()).isEqualTo(port);
    }

    @Test
    void macaroonFile() {
        File macaroonFile = new File("tmp");
        lndConfiguration.setMacaroonFile(macaroonFile);
        assertThat(lndConfiguration.getMacaroonFile()).isEqualTo(macaroonFile);
    }

    @Test
    void certFile() {
        File certFile = new File("tmp");
        lndConfiguration.setCertFile(certFile);
        assertThat(lndConfiguration.getCertFile()).isEqualTo(certFile);
    }
}