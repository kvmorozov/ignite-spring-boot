package ru.kmorozov.ignite.spring;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import ru.kmorozov.ignite.spring.annotations.IgniteResource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static org.apache.ignite.internal.IgniteNodeAttributes.ATTR_GRID_NAME;

/**
 * Created by sbt-morozov-kv on 22.09.2016.
 */

public class IgniteSpringBootConfiguration {

    private Map<String, List<IgniteHolder>> igniteMap = new HashMap<>();
    private boolean initialized = false;
    private DefaultIgniteProperties props;

    IgniteSpringBootConfiguration(DefaultIgniteProperties props) {
        this.props = props;
    }

    private static final class IgniteHolder {
        IgniteHolder(IgniteConfiguration config, Ignite ignite) {
            this.config = config;
            this.ignite = ignite;
        }

        IgniteHolder(IgniteConfiguration config) {
            this(config, null);
        }

        IgniteConfiguration config;
        Ignite ignite;
    }

    private void initIgnition() {
        List<IgniteConfiguration> igniteConfigurations = new ArrayList<>();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] igniteResources = resolver.getResources(props.getConfigPath());
            List<String> igniteResourcesPaths = new ArrayList<>();
            for (Resource igniteXml : igniteResources)
                igniteResourcesPaths.add(igniteXml.getFile().getPath());

            FileSystemXmlApplicationContext xmlContext =
                    new FileSystemXmlApplicationContext(igniteResourcesPaths.stream().toArray(String[]::new));

            igniteConfigurations.addAll(xmlContext.getBeansOfType(IgniteConfiguration.class).values());

            for (IgniteConfiguration config : igniteConfigurations) {
                List<IgniteHolder> configs = igniteMap.get(config.getGridName());
                if (configs == null) {
                    configs = new ArrayList<>();
                    igniteMap.put(config.getGridName(), configs);
                }

                configs.add(new IgniteHolder(config));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Ignite getIgnite(IgniteResource[] igniteProps) {
        if (!initialized) {
            initIgnition();
            initialized = true;
        }

        String gridName = igniteProps == null || igniteProps.length == 0
                ? null
                : igniteProps[0].gridName();
        IgniteResource gridResource = igniteProps == null || igniteProps.length == 0
                ? null
                : igniteProps[0];

        List<IgniteHolder> configs = igniteMap.get(gridName);
        Ignite ignite;

        if (configs == null) {
            IgniteConfiguration defaultIgnite = getDefaultIgniteConfig(gridResource);
            ignite = Ignition.start(defaultIgnite);
            List<IgniteHolder> holderList = new ArrayList<>();
            holderList.add(new IgniteHolder(defaultIgnite, ignite));
            igniteMap.put(gridName, holderList);
        } else {
            IgniteHolder igniteHolder = configs.get(0);
            if (igniteHolder.ignite == null) {
                igniteHolder.ignite = Ignition.start(igniteHolder.config);
            }
            ignite = igniteHolder.ignite;
        }

        if (props.isUseSameServerNames()) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(Ignite.class);
            enhancer.setCallback(new IgniteHandler(ignite));

            ignite = (Ignite) enhancer.create();
        }

        return ignite;
    }

    private class IgniteHandler implements InvocationHandler {

        private Ignite ignite;

        IgniteHandler(Ignite ignite) {
            this.ignite = ignite;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.getName().equals("compute")
                    ? ignite.compute(ignite.cluster()
                    .forAttribute(ATTR_GRID_NAME, ignite.configuration().getGridName())
                    .forServers())
                    : method.invoke(ignite, args);
        }
    }

    private IgniteConfiguration getDefaultIgniteConfig(IgniteResource gridResource) {
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        igniteConfiguration.setGridName(getGridName(gridResource));
        igniteConfiguration.setClientMode(getClientMode(gridResource));
        igniteConfiguration.setPeerClassLoadingEnabled(getPeerClassLoadingEnabled(gridResource));

        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList(getIpDiscoveryRange(gridResource)));
        tcpDiscoverySpi.setIpFinder(ipFinder);
        tcpDiscoverySpi.setLocalAddress(getLocalAddress(gridResource));
        igniteConfiguration.setDiscoverySpi(tcpDiscoverySpi);

        TcpCommunicationSpi communicationSpi = new TcpCommunicationSpi();
        communicationSpi.setLocalAddress(props.getLocalAddress());
        igniteConfiguration.setCommunicationSpi(communicationSpi);

        return igniteConfiguration;
    }

    private String getGridName(IgniteResource gridResource) {
        return gridResource == null
                ? props.getGridName()
                : ifNullOrEmpty(gridResource.gridName(), props.getGridName());
    }

    private boolean getClientMode(IgniteResource gridResource) {
        return gridResource == null
                ? props.isClientMode()
                : gridResource.clientMode();
    }

    private boolean getPeerClassLoadingEnabled(IgniteResource gridResource) {
        return gridResource == null ? props.isPeerClassLoadingEnabled() : gridResource.peerClassLoadingEnabled();
    }

    private String getIpDiscoveryRange(IgniteResource gridResource) {
        return gridResource == null
                ? props.getGridName()
                : ifNullOrEmpty(gridResource.ipDiscoveryRange(), props.getIpDiscoveryRange());
    }

    private String getLocalAddress(IgniteResource gridResource) {
        return gridResource == null
                ? props.getGridName()
                : ifNullOrEmpty(gridResource.localAddress(), props.getLocalAddress());
    }

    private String ifNullOrEmpty(String value, String defaultValue) {
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }
}