package com.artem.brewery.client;

import io.restassured.specification.RequestSpecification;
import com.artem.brewery.dto.Brewery;

import java.util.List;

import static io.restassured.RestAssured.given;

public class OpenBreweryClient {

    private final RequestSpecification spec;

    public OpenBreweryClient(RequestSpecification spec) {
        this.spec = spec;
    }

    public List<Brewery> searchBreweries(SearchBreweriesRequest request) {
        return given()
                .spec(spec)
                .when()
                .queryParam("query", request.getQuery())
                .queryParam("page", request.getPage())
                .queryParam("per_page", request.getPerPage())
                .get("/breweries/search")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("", Brewery.class);
    }
}
