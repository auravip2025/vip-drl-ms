package com.example.kyc.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Individual Product KYC Request")
public class IndividualProductKycRequest {
    
    @Schema(description = "Product type for individual customer", example = "SAVINGS", required = true,
            allowableValues = {"SAVINGS", "CURRENT", "FIXED_DEPOSIT", "INVESTMENT", "LOAN", "CREDIT_CARD"})
    private String product;

    @Schema(description = "Country of residence/operation", example = "SINGAPORE")
    private String country;

    // Constructors
    public IndividualProductKycRequest() {}

    public IndividualProductKycRequest(String product, String country) {
        this.product = product;
        this.country = country;
    }

    // Getters and Setters
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
