package com.example.kyc.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Corporate KYC Request")
public class CorporateKycRequest {
    
    @Schema(description = "Product type for corporate customer", example = "FX", required = true,
            allowableValues = {"CASA", "FX", "TRADING"})
    private String product;

    // Constructors
    public CorporateKycRequest() {}

    public CorporateKycRequest(String product) {
        this.product = product;
    }

    // Getters and Setters
    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}
