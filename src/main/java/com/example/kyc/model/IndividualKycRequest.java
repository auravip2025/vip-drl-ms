package com.example.kyc.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Individual KYC Request")
public class IndividualKycRequest {
    
    @Schema(description = "Customer type", example = "INDIVIDUAL", required = true,
            allowableValues = {"INDIVIDUAL", "CORPORATE", "SOLE_PROPRIETOR", "PARTNERSHIP", "TRUST", "FOREIGNER"})
    private String customerType;
    
    @Schema(description = "Account type", example = "SAVINGS", required = true,
            allowableValues = {"SAVINGS", "CURRENT", "FIXED_DEPOSIT", "INVESTMENT", "LOAN", "CREDIT_CARD"})
    private String accountType;
    
    @Schema(description = "Customer nationality", example = "SINGAPORE", required = true)
    private String nationality;
    
    @Schema(description = "Is the customer a Politically Exposed Person?", example = "false", required = true)
    private Boolean pep;

    @Schema(description = "Country of residence/operation", example = "SINGAPORE")
    private String country;

    // Constructors
    public IndividualKycRequest() {}

    public IndividualKycRequest(String customerType, String accountType, String nationality, Boolean pep, String country) {
        this.customerType = customerType;
        this.accountType = accountType;
        this.nationality = nationality;
        this.pep = pep;
        this.country = country;
    }

    // Getters and Setters
    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Boolean getPep() {
        return pep;
    }

    public void setPep(Boolean pep) {
        this.pep = pep;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
