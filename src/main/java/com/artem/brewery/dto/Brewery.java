package com.artem.brewery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Brewery {

    private String id;
    private String name;
    private String brewery_type;

    private String address_1;
    private String address_2;
    private String address_3;

    private String city;
    private String state_province;
    private String postal_code;
    private String country;

    private Double longitude;
    private Double latitude;

    private String phone;
    private String website_url;
}
