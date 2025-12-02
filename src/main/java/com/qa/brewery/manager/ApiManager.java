package com.qa.brewery.manager;

import io.restassured.specification.RequestSpecification;
import com.qa.brewery.client.OpenBreweryClient;

public class ApiManager {

    private final RequestSpecification spec;

    private OpenBreweryClient breweryClient;

    public ApiManager(RequestSpecification spec) {
        this.spec = spec;
    }

    public OpenBreweryClient breweries() {
        if (breweryClient == null) {
            breweryClient = new OpenBreweryClient(spec);
        }
        return breweryClient;
    }
}
