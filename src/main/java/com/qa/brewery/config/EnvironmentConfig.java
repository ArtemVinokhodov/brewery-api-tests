package com.qa.brewery.config;

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "classpath:env-${env}.properties",
        "classpath:env.properties"
})
public interface EnvironmentConfig extends Config {

    @Key("base.url")
    @DefaultValue("https://api.openbrewerydb.org/v1")
    String baseUrl();

    @Key("api.timeout")
    @DefaultValue("10000")
    int timeout();

    @Key("api.retries")
    @DefaultValue("3")
    int retries();
}

