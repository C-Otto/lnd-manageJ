package de.cotto.lndmanagej;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

@Component
@SuppressWarnings({"PMD.DataClass", "unused"})
@ConfigurationProperties(prefix = "lndmanagej")
public class LndConfiguration {
    private File macaroonFile;
    private File certFile;
    private String host;
    private int port;

    public LndConfiguration() {
        // default constructor
    }

    public File getCertFile() {
        return Objects.requireNonNull(certFile);
    }

    public File getMacaroonFile() {
        return Objects.requireNonNull(macaroonFile);
    }

    public String getHost() {
        return Objects.requireNonNull(host);
    }

    public int getPort() {
        return port;
    }

    public void setMacaroonFile(File macaroonFile) {
        this.macaroonFile = macaroonFile;
    }

    public void setCertFile(File certFile) {
        this.certFile = certFile;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}