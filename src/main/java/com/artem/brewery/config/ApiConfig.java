package com.artem.brewery.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;

@Getter
public class ApiConfig {

    private static final String BASE_URL_PROPERTY = "base.url";

    private static ApiConfig instance;

    private final RequestSpecification requestSpec;
    private final String baseUrl;

    private ApiConfig() {
        ConfigLoader configLoader = new ConfigLoader();
        this.baseUrl = configLoader.getProperty(BASE_URL_PROPERTY);

        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("Base URL is not configured. Check your env.properties file.");
        }

        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
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
}
