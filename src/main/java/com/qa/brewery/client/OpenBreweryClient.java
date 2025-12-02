package com.qa.brewery.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class OpenBreweryClient {

    public static final String PARAM_QUERY = "query";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PER_PAGE = "per_page";
    public static final String PATH_SEARCH = "/breweries/search";

    private final RequestSpecification spec;

    public OpenBreweryClient(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response searchBreweries(SearchBreweriesRequest request) {
        return given()
                .spec(spec)
                .when()
                .queryParam(PARAM_QUERY, request.getQuery())
                .queryParam(PARAM_PAGE, request.getPage())
                .queryParam(PARAM_PER_PAGE, request.getPerPage())
                .get(PATH_SEARCH);
    }
}
