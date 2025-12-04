# OpenBreweryDB API Test Automation  
Java | REST | Assured | TestNG

This project contains automated tests for the **"Search Breweries"** endpoint of the OpenBreweryDB public API.  
It is implemented using **Java 17**, **REST Assured**, **Maven**, **TestNG**, **AssertJ**, and **Lombok**.

---

## 1. Covered API Method
### **Search Breweries**
Documentation:  
https://www.openbrewerydb.org/documentation

Endpoint:  
`GET /v1/breweries/search`

Supported query parameters:

| Parameter | Description |
|----------|-------------|
| `query` | Search term used for brewery name matching |
| `page` | Page number (pagination) |
| `per_page` | Items per page (default: 50, maximum currently returns up to ~1000 depending on dataset) |

---

## 2. Implemented Test Scenarios (10 scenarios total)

Below are the scenarios implemented in code. The assignment required 5 core scenarios, additionally 5 negative scenarios were implemented to demonstrate comprehensive edge case testing:

### **1. Search by existing substring returns non-empty results**
- Query: `"dog"`
- Verifies that the search returns results and respects `per_page` limit (5 items max).
- Checks that at least one brewery name contains the search term (case-insensitive).
- Note: API searches across multiple fields (name, city, state), so not all results may have "dog" in the name.

### **2. Search with no matching term returns empty result**
- Query: `"qwerty12345"` (random non-existing term)
- Verifies that API returns an empty array when no matches are found.

### **3. `per_page` parameter limits the number of returned items**
- Query: `"brew"` with `per_page = 3`
- Validates that API respects the `per_page` parameter and returns no more than requested items.

### **4. Pagination: different pages return different result sets**
- Query: `"beer"` with `page = 1` and `page = 2`, each with `per_page = 5`
- Ensures that the second page does not fully duplicate the first page.
- Test is resilient: if the second page is empty (not enough data), the test passes gracefully.

### **5. Large `per_page` request does not exceed the requested size**
- Query: `"brew"` with `per_page = 200`
- Validates a general API invariant: even with large `per_page` values, API never returns more items than requested.

### **6. Empty query parameter handling**
- Query: `""` (empty string)
- Validates that API handles empty query gracefully without errors.

### **7. Special characters in query parameter**
- Query: `"!@#$%^&*()"`
- Validates that API properly handles special characters without breaking.

### **8. Zero value for page parameter**
- Query: `"beer"` with `page = 0`
- Validates that API handles edge case of page=0 gracefully.

### **9. Zero value for per_page parameter**
- Query: `"beer"` with `per_page = 0`
- Validates that API handles edge case of per_page=0 without errors.

### **10. Negative value for page parameter**
- Query: `"beer"` with `page = -1`
- Validates that API handles negative pagination values gracefully.

---

## 3. Additional Recommended Scenarios (not implemented, listed as requested)

These scenarios could be included in an extended test suite:

### **Functional**
- Case-insensitive search verification.
- Search with unicode characters (e.g., `"łódź"`, `"kühn"`, emoji).
- Search by exact match vs partial matching.
- Search with URL-encoded characters.

### **Pagination**
- Boundary values:  
  `page = 0`, `page = -1`, `page = very large number`.
- Verify consistency between pages (no duplicates across pages).

### **per_page behavior**
- `per_page = 1` returns exactly 1 item (if available).
- `per_page = 0` / negative values.
- API capping logic if the API enforces limits.

### **Response structure**
- All required fields exist in BreweryDTO.
- Fields ignored safely when API adds new fields (`@JsonIgnoreProperties`).

### **Performance**
- Response time under acceptable threshold (e.g., <1s).

---

## 4. Thoughts on Automating “List Breweries”

Endpoint:  
`GET /v1/breweries`

This method returns full brewery lists with pagination and supports multiple filters.

### **Suggested Test Automation Approach**

**Note:** The following are recommended approaches for automating the "List Breweries" method. The current project already implements the layered architecture for "Search Breweries".

#### 1. **Layered Architecture**
- **Client layer**: Encapsulates REST Assured calls.
- **DTOs**: BreweryDTO model mapped with Jackson.
- **Config layer**: Base URI, common request spec, logging.
- **Test layer**: Test scenarios only, no direct REST Assured usage.

(This approach is already implemented in this project.)

#### 2. **Test Design Techniques**

**Already implemented for Search Breweries:**
- **Equivalence Partitioning**: Valid/invalid pages (0, -1, 1, 2), valid/invalid per_page values
- **Boundary Value Analysis**: per_page boundaries (3, 200), edge cases (page=0, per_page=0)
- **Negative Testing**: Empty query, special characters, zero/negative pagination values

**Planned for List Breweries (future coverage):**
- **Data-Driven Testing**: Different filter combinations (`by_city`, `by_type`, `by_state`, `by_country`)
- **Schema Validation**: Validate response structure with JSON schema
- **Sorting Validation**: Test endpoint ordering logic if applicable
- **Extended Negative Cases**: Invalid parameter types, missing parameters, unsupported filters

---

## 5. Estimated Effort for Full Automation

| Task | Estimated Time |
|------|----------------|
| Implement base framework (specs, config, client) | 2 hours |
| Add coverage for all parameters (`by_city`, `by_type`, etc.) | 5 hours |
| Add pagination boundary tests | 2 hours |
| Negative testing scenarios (invalid params, edge cases) | 1.5 hours |
| Response validation and schema checks | 1 hour |
| Full documentation + cleanup | 0.5 hours |

**Total estimated effort: 12 hours (1.5 days)**

---

## 6. Environment Configuration

The project uses **Owner library** for type-safe configuration management.

Configuration files are located in `src/main/resources/`:

- `env.properties` - Default environment (production)
- `env-dev.properties` - Development environment
- `env-staging.properties` - Staging environment
- `env-prod.properties` - Production environment

### **Switching Environments**

**Option 1: Using Maven**
```bash
# Run with default environment (production)
mvn clean test

# Run with specific environment
mvn clean test -Denv=dev
mvn clean test -Denv=staging
mvn clean test -Denv=prod
```

**Option 2: Using IntelliJ IDEA**
1. Run -> Edit Configurations
2. Add VM options: `-Denv=dev`
3. Click Apply and run tests

### **Configuration Properties**

Each environment file contains:
```properties
base.url=https://api.openbrewerydb.org/v1
```

### **Owner Library Benefits**

-  Type-safe access to properties (no string keys in code)
-  Default values via `@DefaultValue` annotation
-  Automatic type conversion (String → int, boolean, etc.)
-  Multiple sources support (system properties, files)
-  Hot reload capability

---

## 7. How to Run Tests

### **Using Maven (with TestNG Suite)**
```bash
# Default environment - runs all tests via testng.xml
mvn clean test

# Specific environment
mvn clean test -Denv=dev
```

### **Using IntelliJ IDEA**

**Option 1: Run entire suite**
1. Navigate to `src/test/resources/testng.xml`
2. Right-click and select **Run**

**Option 2: Run individual test class**
1. Navigate to `src/test/java/com/qa/brewery/tests/SearchBreweriesTest.java`
2. Right-click the class and select **Run** with TestNG
3. (Optional) Add `-Denv=dev` in VM options for specific environment

---

## 8. Project Structure
```
src
 ├─ main
 │   ├─ java
 │   │   └─ com.qa.brewery
 │   │        ├─ client
 │   │        │    └─ OpenBreweryClient.java
 │   │        ├─ config
 │   │        │    ├─ ApiConfig.java
 │   │        │    └─ EnvironmentConfig.java
 │   │        ├─ dto
 │   │        │    ├─ BreweryDTO.java
 │   │        │    └─ BrewerySearchParams.java
 │   │        └─ manager
 │   │             └─ ApiManager.java
 │   └─ resources
 │        ├─ env.properties
 │        ├─ env-dev.properties
 │        ├─ env-staging.properties
 │        ├─ env-prod.properties
 │        └─ log4j2.xml
 └─ test
     ├─ java
     │   ├─ com.qa.brewery.steps
     │   │    └─ BreweriesApiSteps.java
     │   └─ com.qa.brewery.tests
     │        └─ SearchBreweriesTest.java
     └─ resources
          └─ testng.xml
```

---

## 9. Allure Reporting

The project is integrated with **Allure Framework** for rich test reporting with automatic request/response attachments.

### **Generate and View Allure Report**

```bash
# Run tests and generate Allure results
mvn clean test

# Generate and open Allure report in browser
mvn allure:serve

# Or generate static HTML report
mvn allure:report
# Report will be in: target/site/allure-maven-plugin/index.html
```

### **Allure Features**

-  **Automatic Request/Response Logging** - All API calls are attached to the report
-  **Test Categorization** - Tests grouped by Epic, Feature, Story
-  **Severity Levels** - Critical and Normal test priorities
-  **Detailed Steps** - Each API call shown as a separate step
-  **Rich Descriptions** - Clear test objectives and expected results
-  **Timeline View** - Visual representation of test execution
-  **Retries & History** - Track test stability over time

### **Allure Annotations Used**

```java
@Epic("OpenBreweryDB API")           // Top-level feature group
@Feature("Search Breweries")         // Feature under test
@Story("Positive Search Scenarios")  // User story
@Severity(SeverityLevel.CRITICAL)    // Test priority
@Description("Test description...")  // Detailed description
@Step("Step description...")         // Method-level step
```

---

## 10. Technologies Used

- Java 17
- REST Assured
- TestNG
- Maven
- Lombok
- AssertJ
- Jackson
- Owner (for configuration management)
- Log4j2 (for logging)
- Allure Framework (for reporting)

## 11. Notes

OpenBreweryDB is a public API, and field structure may change.

DTOs are configured to ignore unknown fields to keep tests stable.

per_page real behavior may differ from documentation due to dataset size; tests validate functional invariants rather than hardcoded numeric limits.

If you have questions or want to extend the project - feel free to ask.
