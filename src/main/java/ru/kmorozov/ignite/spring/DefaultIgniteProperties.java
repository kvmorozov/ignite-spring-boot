package ru.kmorozov.ignite.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by sbt-morozov-kv on 22.09.2016.
 */

@ConfigurationProperties(prefix = "ignite.configuration.default")
@PropertySource("classpath:application.properties")
public class DefaultIgniteProperties {

    private String configPath;

    private String gridName;

    private boolean clientMode;

    private boolean peerClassLoadingEnabled;

    private String localAddress;

    private String ipDiscoveryRange;

    private boolean useSameServerNames;

    public boolean isClientMode() {
        return clientMode;
    }

    public boolean isPeerClassLoadingEnabled() {
        return peerClassLoadingEnabled;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public String getIpDiscoveryRange() {
        return ipDiscoveryRange;
    }

    public void setClientMode(boolean clientMode) {
        this.clientMode = clientMode;
    }

    public void setPeerClassLoadingEnabled(boolean peerClassLoadingEnabled) {
        this.peerClassLoadingEnabled = peerClassLoadingEnabled;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public void setIpDiscoveryRange(String ipDiscoveryRange) {
        this.ipDiscoveryRange = ipDiscoveryRange;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public String getGridName() {
        return gridName;
    }

    public void setGridName(String gridName) {
        this.gridName = gridName;
    }

    public boolean isUseSameServerNames() {
        return useSameServerNames;
    }

    public void setUseSameServerNames(boolean useSameServerNames) {
        this.useSameServerNames = useSameServerNames;
    }
}
