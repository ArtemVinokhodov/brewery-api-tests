package com.qa.brewery.tests;

import com.qa.brewery.dto.BrewerySearchParams;
import com.qa.brewery.config.ApiConfig;
import com.qa.brewery.dto.Brewery;
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
    @Description("Verify that search returns results when querying with an existing brewery name and respects per_page limit")
    public void searchByExistingName() {
        BrewerySearchParams params = BrewerySearchParams.builder()
                .query("dog")
                .page(1)
                .perPage(5)
                .build();

        List<Brewery> result = breweriesSteps.searchBreweries(params);

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
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that search returns empty list when querying with non-existing term")
    public void searchByNonExistingQuery() {
        BrewerySearchParams params = BrewerySearchParams.builder()
                .query("qwerty12345")
                .page(1)
                .perPage(10)
                .build();

        List<Brewery> result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("Search with a non-existing term should return an empty list")
                .isEmpty();
    }

    @Test
    @Story("Pagination")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that API respects per_page parameter and limits the number of returned results")
    public void searchWithPerPageLimit() {
        BrewerySearchParams params = BrewerySearchParams.builder()
                .query("brew")
                .page(1)
                .perPage(3)
                .build();

        List<Brewery> result = breweriesSteps.searchBreweries(params);

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
        BrewerySearchParams firstPageParams = BrewerySearchParams.builder()
                .query("beer")
                .page(1)
                .perPage(5)
                .build();

        BrewerySearchParams secondPageParams = BrewerySearchParams.builder()
                .query("beer")
                .page(2)
                .perPage(5)
                .build();

        List<Brewery> firstPage = breweriesSteps.searchBreweries(firstPageParams);
        List<Brewery> secondPage = breweriesSteps.searchBreweries(secondPageParams);

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
    @Story("Boundary Testing")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles large per_page values correctly and does not exceed requested limit")
    public void searchWithLargePerPage() {
        int requestedPerPage = 200;

        BrewerySearchParams params = BrewerySearchParams.builder()
                .query("brew")
                .page(1)
                .perPage(requestedPerPage)
                .build();

        List<Brewery> result = breweriesSteps.searchBreweries(params);

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
        BrewerySearchParams params = BrewerySearchParams.builder()
                .query("")
                .page(1)
                .perPage(10)
                .build();

        var response = breweriesSteps.searchBreweriesRaw(params);

        assertThat(response.statusCode())
                .as("API should return 200 status for empty query")
                .isEqualTo(200);

        assertThat(response.body().asString())
                .as("API should return welcome message when query is empty")
                .contains("Welcome to the Breweries API")
                .containsAnyOf("https://www.openbrewerydb.org/documentation", "https:\\/\\/www.openbrewerydb.org\\/documentation");
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles special characters in query parameter")
    public void searchWithSpecialCharacters() {
        BrewerySearchParams params = BrewerySearchParams.builder()
                .query("!@#$%^&*()")
                .page(1)
                .perPage(10)
                .build();

        List<Brewery> result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("API should handle special characters without errors and return empty list")
                .isEmpty();
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles zero value for page parameter")
    public void searchWithZeroPage() {
        BrewerySearchParams params = BrewerySearchParams.builder()
                .query("beer")
                .page(0)
                .perPage(10)
                .build();

        var response = breweriesSteps.searchBreweriesRaw(params);

        assertThat(response.statusCode())
                .as("API should return 200 status for page=0")
                .isEqualTo(200);

        List<Brewery> result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("API should return empty list or valid results for page=0")
                .isNotNull()
                .allMatch(brewery -> brewery.getId() != null);
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles zero value for per_page parameter")
    public void searchWithZeroPerPage() {
        BrewerySearchParams params = BrewerySearchParams.builder()
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
                .containsAnyOf("https://www.openbrewerydb.org/documentation", "https:\\/\\/www.openbrewerydb.org\\/documentation");
    }

    @Test
    @Story("Negative Search Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify that API handles negative value for page parameter")
    public void searchWithNegativePage() {
        BrewerySearchParams params = BrewerySearchParams.builder()
                .query("beer")
                .page(-1)
                .perPage(10)
                .build();

        var response = breweriesSteps.searchBreweriesRaw(params);

        assertThat(response.statusCode())
                .as("API should return 200 status for negative page")
                .isEqualTo(200);

        List<Brewery> result = breweriesSteps.searchBreweries(params);

        assertThat(result)
                .as("API should return empty list or valid results for negative page")
                .isNotNull()
                .allMatch(brewery -> brewery.getId() != null && brewery.getName() != null);
    }
}
