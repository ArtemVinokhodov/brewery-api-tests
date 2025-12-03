package com.qa.brewery.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BrewerySearchParams {
    private String query;
    private Integer page;
    private Integer perPage;
}
