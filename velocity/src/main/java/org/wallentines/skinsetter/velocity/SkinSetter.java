package org.wallentines.skinsetter.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.apache.logging.log4j.Logger;

@Plugin(id = "skinsetter", name = "SkinSetter", version = "1.0.0", authors = {"M1dnight_Ninja"})
public class SkinSetter {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public SkinSetter(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        ConfigSection sec = new ConfigSection();
    }

}
