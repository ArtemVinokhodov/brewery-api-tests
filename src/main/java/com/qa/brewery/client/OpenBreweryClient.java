package com.qa.brewery.client;

import com.qa.brewery.dto.BrewerySearchParams;
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

    public Response searchBreweries(BrewerySearchParams params) {
        return given()
                .spec(spec)
                .when()
                .queryParam(PARAM_QUERY, params.getQuery())
                .queryParam(PARAM_PAGE, params.getPage())
                .queryParam(PARAM_PER_PAGE, params.getPerPage())
                .get(PATH_SEARCH);
    }
}
