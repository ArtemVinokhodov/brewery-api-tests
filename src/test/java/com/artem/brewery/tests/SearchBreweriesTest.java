package com.artem.brewery.tests;

import com.artem.brewery.client.OpenBreweryClient;
import com.artem.brewery.client.SearchBreweriesRequest;
import com.artem.brewery.config.ApiConfig;
import com.artem.brewery.dto.Brewery;
import com.artem.brewery.manager.ApiManager;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchBreweriesTest {

    private ApiManager api;
    private OpenBreweryClient breweries;

    @BeforeClass
    public void setUp() {
        api = new ApiManager(ApiConfig.getInstance().getRequestSpec());
        breweries = api.breweries();
    }

    @Test
    public void search_withExistingName_returnsResultsWithCorrectSubstring() {
        SearchBreweriesRequest request = SearchBreweriesRequest.builder()
                .query("dog")
                .page(1)
                .perPage(5)
                .build();

        List<Brewery> result = breweries.searchBreweries(request);

        assertThat(result)
                .as("Search should return some results for a popular query 'dog'")
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(5);

        boolean anyMatchByName = result.stream()
                .map(Brewery::getName)
                .filter(Objects::nonNull)
                .anyMatch(name -> name.toLowerCase().contains("dog"));

        assertThat(anyMatchByName)
                .as("At least one brewery name should contain the search term 'dog'")
                .isTrue();
    }

    @Test
    public void search_withNonExistingQuery_returnsEmptyList() {
        SearchBreweriesRequest request = SearchBreweriesRequest.builder()
                .query("qwerty12345")
                .page(1)
                .perPage(10)
                .build();

        List<Brewery> result = breweries.searchBreweries(request);

        assertThat(result)
                .as("Search with a non-existing term should return an empty list")
                .isEmpty();
    }

    @Test
    public void search_withSmallPerPage_limitsNumberOfResults() {
        SearchBreweriesRequest request = SearchBreweriesRequest.builder()
                .query("brew")
                .page(1)
                .perPage(3)
                .build();

        List<Brewery> result = breweries.searchBreweries(request);

        assertThat(result)
                .as("API should respect per_page parameter")
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(3);
    }

    @Test
    public void search_withDifferentPages_returnsDifferentResultSets() {
        SearchBreweriesRequest firstPageRequest = SearchBreweriesRequest.builder()
                .query("beer")
                .page(1)
                .perPage(5)
                .build();

        SearchBreweriesRequest secondPageRequest = SearchBreweriesRequest.builder()
                .query("beer")
                .page(2)
                .perPage(5)
                .build();

        List<Brewery> firstPage = breweries.searchBreweries(firstPageRequest);
        List<Brewery> secondPage = breweries.searchBreweries(secondPageRequest);

        assertThat(firstPage)
                .as("First page should not be empty for a popular term")
                .isNotEmpty();

        Set<String> firstIds = firstPage.stream()
                .map(Brewery::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> secondIds = secondPage.stream()
                .map(Brewery::getId)
                .filter(Objects::nonNull)
                .toList();

        boolean isFullDuplicate =
                !secondIds.isEmpty() && firstIds.containsAll(secondIds);

        assertThat(isFullDuplicate)
                .as("Second page should not fully duplicate the first page")
                .isFalse();
    }

    @Test
    public void search_withTooLargePerPage_doesNotReturnMoreThanRequested() {

        int requestedPerPage = 200;

        SearchBreweriesRequest request = SearchBreweriesRequest.builder()
                .query("brew")
                .page(1)
                .perPage(requestedPerPage)
                .build();

        List<Brewery> result = breweries.searchBreweries(request);

        assertThat(result)
                .as("API should not return more items than requested per_page")
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(requestedPerPage);
    }
}
