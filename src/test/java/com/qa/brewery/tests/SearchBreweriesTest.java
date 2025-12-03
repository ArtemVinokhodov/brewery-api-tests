package com.qa.brewery.tests;

import com.qa.brewery.dto.BrewerySearchParams;
import com.qa.brewery.config.ApiConfig;
import com.qa.brewery.dto.BreweryDTO;
import com.qa.brewery.manager.ApiManager;
import com.qa.brewery.steps.BreweriesApiSteps;
import io.qameta.allure.*;
import lombok.extern.log4j.Log4j2;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@Epic("OpenBreweryDB API")
@Feature("Search Breweries")
public class SearchBreweriesTest {

    private BreweriesApiSteps breweriesSteps;

    @BeforeClass
    public void setUp() {
        log.info("Setting up API client for OpenBreweryDB tests");
        ApiManager api = new ApiManager(ApiConfig.getInstance().getRequestSpec());
        breweriesSteps = new BreweriesApiSteps(api.breweries());
        log.info("API client initialized successfully");
    }

    @Test
    @Story("Positive Search Scenarios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that search returns results when querying with an existing term and respects per_page limit. API searches across multiple fields: name, city, state, address.")
    public void searchByExistingName() {
        var params = BrewerySearchParams.builder()
                .query("dog")
                .page(1)
                .perPage(5)
                .build();

        var result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("Search should return some results for a popular query 'dog'")
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(5);

        var anyMatch = result.stream()
                .anyMatch(brewery ->
                    brewery.getName() != null && brewery.getName().toLowerCase().contains("dog")
                );

        assertThat(anyMatch)
                .as("At least one brewery name should contain the search term 'dog'")
                .isTrue();
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that search returns empty list when querying with non-existing term")
    public void searchByNonExistingQuery() {
        var params = BrewerySearchParams.builder()
                .query("qwerty12345")
                .page(1)
                .perPage(10)
                .build();

        var result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("Search with a non-existing term should return an empty list")
                .isEmpty();
    }

    @Test
    @Story("Pagination")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that API respects per_page parameter and limits the number of returned results")
    public void searchWithPerPageLimit() {
        var params = BrewerySearchParams.builder()
                .query("brew")
                .page(1)
                .perPage(3)
                .build();

        var result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("API should respect per_page parameter")
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(3);
    }

    @Test
    @Story("Pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that different pages return different result sets and pagination works correctly")
    public void searchWithDifferentPages() {
        var firstPageParams = BrewerySearchParams.builder()
                .query("beer")
                .page(1)
                .perPage(5)
                .build();

        var secondPageParams = BrewerySearchParams.builder()
                .query("beer")
                .page(2)
                .perPage(5)
                .build();

        var firstPage = breweriesSteps.searchBreweries(firstPageParams);
        var secondPage = breweriesSteps.searchBreweries(secondPageParams);

        assertThat(firstPage)
                .as("First page should not be empty for a popular term")
                .isNotEmpty();

        var firstIds = firstPage.stream()
                .map(BreweryDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var secondIds = secondPage.stream()
                .map(BreweryDTO::getId)
                .filter(Objects::nonNull)
                .toList();

        var isFullDuplicate = !secondIds.isEmpty() && firstIds.containsAll(secondIds);

        assertThat(isFullDuplicate)
                .as("Second page should not fully duplicate the first page")
                .isFalse();
    }

    @Test
    @Story("Boundary Testing")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles large per_page values correctly and does not exceed requested limit")
    public void searchWithLargePerPage() {
        var requestedPerPage = 200;

        var params = BrewerySearchParams.builder()
                .query("brew")
                .page(1)
                .perPage(requestedPerPage)
                .build();

        var result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("API should not return more items than requested per_page")
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(requestedPerPage);
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles empty query parameter gracefully")
    public void searchWithEmptyQuery() {
        var params = BrewerySearchParams.builder()
                .query("")
                .page(1)
                .perPage(10)
                .build();

        var response = breweriesSteps.searchBreweriesRaw(params);
        var responseBody = response.body().asString();

        assertThat(response.statusCode())
                .as("API should return 200 status for empty query")
                .isEqualTo(200);

        assertThat(responseBody)
                .as("API should return welcome message when query is empty")
                .contains("Welcome to the Breweries API")
                .matches("(?i).*openbrewerydb\\.org.*documentation.*");
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles special characters in query parameter")
    public void searchWithSpecialCharacters() {
        var params = BrewerySearchParams.builder()
                .query("!@#$%^&*()")
                .page(1)
                .perPage(10)
                .build();

        var result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("API should handle special characters without errors and return empty list")
                .isEmpty();
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles zero value for page parameter gracefully without errors")
    public void searchWithZeroPage() {
        var params = BrewerySearchParams.builder()
                .query("beer")
                .page(0)
                .perPage(10)
                .build();

        var result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("API should return at least one brewery for page=0")
                .isNotEmpty()
                .hasSizeGreaterThan(0);
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles zero value for per_page parameter")
    public void searchWithZeroPerPage() {
        var params = BrewerySearchParams.builder()
                .query("beer")
                .page(1)
                .perPage(0)
                .build();

        var response = breweriesSteps.searchBreweriesRaw(params);

        assertThat(response.statusCode())
                .as("API should return 200 status for per_page=0")
                .isEqualTo(200);

        assertThat(response.body().asString())
                .as("API should return welcome message when per_page=0")
                .contains("Welcome to the Breweries API")
                .matches("(?i).*openbrewerydb\\.org.*documentation.*");
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles negative value for page parameter gracefully without errors")
    public void searchWithNegativePage() {
        var params = BrewerySearchParams.builder()
                .query("beer")
                .page(-1)
                .perPage(10)
                .build();

        var result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("API should return at least one brewery for page=-1")
                .isNotEmpty()
                .hasSizeGreaterThan(0);
    }
}
