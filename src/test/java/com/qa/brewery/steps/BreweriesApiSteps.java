package com.qa.brewery.steps;

import com.qa.brewery.client.OpenBreweryClient;
import com.qa.brewery.dto.BrewerySearchParams;
import com.qa.brewery.dto.Brewery;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class BreweriesApiSteps {

    private final OpenBreweryClient client;

    public BreweriesApiSteps(OpenBreweryClient client) {
        this.client = client;
    }

    @Step("Search breweries with query, page and perPage")
    public List<Brewery> searchBreweries(String query, int page, int perPage) {
        log.info("Searching breweries: query='{}', page={}, perPage={}", query, page, perPage);

        BrewerySearchParams params = BrewerySearchParams.builder()
                .query(query)
                .page(page)
                .perPage(perPage)
                .build();

        Response response = client.searchBreweries(params);
        log.info("Response status: {}", response.statusCode());

        response.then().statusCode(200);

        List<Brewery> results = Arrays.asList(response.as(Brewery[].class));
        log.info("Search completed. Found {} breweries", results.size());

        return results;
    }

    @Step("Search breweries with params")
    public List<Brewery> searchBreweries(BrewerySearchParams params) {
        log.info("Searching breweries: query='{}', page={}, perPage={}",
                params.getQuery(), params.getPage(), params.getPerPage());

        Response response = client.searchBreweries(params);
        log.info("Response status: {}", response.statusCode());

        response.then().statusCode(200);

        List<Brewery> results = Arrays.asList(response.as(Brewery[].class));
        log.info("Search completed. Found {} breweries", results.size());

        return results;
    }

    @Step("Get raw response for search breweries")
    public Response searchBreweriesRaw(BrewerySearchParams params) {
        log.info("Searching breweries (raw): query='{}', page={}, perPage={}",
                params.getQuery(), params.getPage(), params.getPerPage());

        Response response = client.searchBreweries(params);
        log.info("Response status: {}", response.statusCode());

        return response;
    }
}

