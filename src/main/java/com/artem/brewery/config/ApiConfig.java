package com.artem.brewery.config;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;

@Getter
public class ApiConfig {

    private static ApiConfig instance;

    private final RequestSpecification requestSpec;

    private ApiConfig() {
        this.requestSpec = new RequestSpecBuilder()
                .setBaseUri("https://api.openbrewerydb.org/v1")
                .setContentType(ContentType.JSON)
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
