package com.artem.brewery.client;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchBreweriesRequest {
    private String query;
    private Integer page;
    private Integer perPage;
}
