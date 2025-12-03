package com.qa.brewery.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import org.aeonbits.owner.ConfigFactory;

@Getter
public class ApiConfig {

    private static ApiConfig instance;

    private final RequestSpecification requestSpec;
    private final EnvironmentConfig config;

    private ApiConfig() {
        this.config = ConfigFactory.create(EnvironmentConfig.class);

        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri(getBaseUrl())
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .log(LogDetail.ALL)
                .build();
    }

    public static synchronized ApiConfig getInstance() {
        if (instance == null) {
            instance = new ApiConfig();
        }
        return instance;
    }

    public String getBaseUrl() {
        return config.baseUrl();
    }
}
