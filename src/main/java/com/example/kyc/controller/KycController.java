package com.example.kyc.controller;

import com.example.kyc.model.CorporateKycRequest;
import com.example.kyc.model.IndividualKycRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * REST Controller that exposes the serverless functions as REST endpoints.
 * This provides a consistent API whether running as a web app or serverless function.
 */
@RestController
@RequestMapping("/api/v1/kyc")
@Tag(name = "KYC Service", description = "Singapore KYC Requirements API - Drools-based rules engine for individual and corporate customers")
public class KycController {

    private final Function<Map<String, Object>, Map<String, Object>> getKycRequirements;
    private final Function<Map<String, Object>, Map<String, Object>> health;
    private final Function<Map<String, Object>, Map<String, Object>> getCustomerTypes;
    private final Function<Map<String, Object>, Map<String, Object>> getAccountTypes;
    private final Function<Map<String, Object>, Map<String, Object>> getCorporateKycRequirements;
    private final Function<Map<String, Object>, Map<String, Object>> getCorporateProductsFunc;

    public KycController(
            Function<Map<String, Object>, Map<String, Object>> getKycRequirements,
            Function<Map<String, Object>, Map<String, Object>> health,
            Function<Map<String, Object>, Map<String, Object>> getCustomerTypes,
            Function<Map<String, Object>, Map<String, Object>> getAccountTypes,
            Function<Map<String, Object>, Map<String, Object>> getCorporateKycRequirements,
            Function<Map<String, Object>, Map<String, Object>> getCorporateProducts) {
        this.getKycRequirements = getKycRequirements;
        this.health = health;
        this.getCustomerTypes = getCustomerTypes;
        this.getAccountTypes = getAccountTypes;
        this.getCorporateKycRequirements = getCorporateKycRequirements;
        this.getCorporateProductsFunc = getCorporateProducts;
    }

    @PostMapping("/requirements")
    @Operation(
            summary = "Get Individual KYC Requirements",
            description = "Returns nested JSON Schema with KYC requirements for individual customers based on customer type, account type, nationality, and PEP status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved KYC requirements",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(name = "Individual SAVINGS Account",
                                    value = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Singapore KYC Form\",\"type\":\"object\",\"properties\":{\"personal_details\":{\"type\":\"object\",\"title\":\"Personal Details\",\"properties\":{\"full_name\":{\"type\":\"string\",\"title\":\"Full Name (as per NRIC/Passport)\"},\"date_of_birth\":{\"type\":\"string\",\"format\":\"date\"},\"gender\":{\"type\":\"string\"},\"nationality\":{\"type\":\"string\"}},\"required\":[\"full_name\",\"date_of_birth\",\"gender\",\"nationality\"]},\"identification\":{},\"contact_details\":{}},\"required\":[\"personal_details\",\"identification\",\"contact_details\"],\"x-metadata\":{\"riskLevel\":\"LOW\",\"totalRequiredFields\":17,\"estimatedProcessingDays\":3,\"categories\":[\"Personal Details\",\"Identification\",\"Contact Details\",\"Employment\",\"Tax Information\",\"Declarations\"]}}")
                    ))
    })
    public Map<String, Object> getRequirements(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Individual KYC request parameters",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IndividualKycRequest.class)
                    )
            )
            @RequestBody IndividualKycRequest request) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("customerType", request.getCustomerType());
        requestMap.put("accountType", request.getAccountType());
        requestMap.put("nationality", request.getNationality());
        requestMap.put("pep", request.getPep());
        return getKycRequirements.apply(requestMap);
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if the service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"status\":\"UP\",\"service\":\"KYC Rules Service\",\"version\":\"1.0.0\"}")
            ))
    public Map<String, Object> healthCheck() {
        return health.apply(null);
    }

    @GetMapping("/customer-types")
    @Operation(summary = "Get Customer Types", description = "List all available customer types for KYC processing")
    @ApiResponse(responseCode = "200", description = "List of customer types",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"customerTypes\":[\"INDIVIDUAL\",\"CORPORATE\",\"SOLE_PROPRIETOR\",\"PARTNERSHIP\",\"TRUST\",\"FOREIGNER\"]}")
            ))
    public Map<String, Object> customerTypes() {
        return getCustomerTypes.apply(null);
    }

    @GetMapping("/account-types")
    @Operation(summary = "Get Account Types", description = "List all available account types for individual customers")
    @ApiResponse(responseCode = "200", description = "List of account types",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"accountTypes\":[\"SAVINGS\",\"CURRENT\",\"FIXED_DEPOSIT\",\"INVESTMENT\",\"LOAN\",\"CREDIT_CARD\"]}")
            ))
    public Map<String, Object> accountTypes() {
        return getAccountTypes.apply(null);
    }

    @PostMapping("/corporate/requirements")
    @Operation(
            summary = "Get Corporate KYC Requirements",
            description = "Returns nested JSON Schema with product-specific KYC requirements for corporate customers (CASA, FX, TRADING)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved corporate KYC requirements",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(name = "Corporate FX Product",
                                    value = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Singapore Corporate KYC Form - FX\",\"type\":\"object\",\"properties\":{\"company_information\":{\"type\":\"object\",\"title\":\"Company Information\",\"properties\":{\"company_name\":{\"type\":\"string\",\"title\":\"Registered Company Name\"},\"uen\":{\"type\":\"string\",\"title\":\"UEN (Unique Entity Number)\",\"pattern\":\"^[0-9]{8,9}[A-Z]$\"}},\"required\":[\"company_name\",\"uen\"]},\"product_fx\":{\"type\":\"object\",\"title\":\"Product Fx\",\"properties\":{\"fx_experience\":{\"type\":\"string\",\"title\":\"FX Trading Experience\"},\"fx_purpose\":{\"type\":\"string\"}},\"required\":[\"fx_experience\",\"fx_purpose\"]}},\"required\":[\"company_information\",\"product_fx\"],\"x-metadata\":{\"product\":\"FX\",\"riskLevel\":\"MEDIUM\",\"totalRequiredFields\":35,\"estimatedProcessingDays\":10,\"categories\":[\"Company Information\",\"Product Fx\",\"Directors Shareholders\"]}}")
                    ))
    })
    public Map<String, Object> getCorporateRequirements(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Corporate KYC request with product type",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CorporateKycRequest.class)
                    )
            )
            @RequestBody CorporateKycRequest request) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("product", request.getProduct());
        return getCorporateKycRequirements.apply(requestMap);
    }

    @GetMapping("/corporate/products")
    @Operation(summary = "Get Corporate Products", description = "List all available corporate banking products with descriptions")
    @ApiResponse(responseCode = "200", description = "List of corporate products with descriptions",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"products\":[{\"code\":\"CASA\",\"name\":\"Current Account Savings Account\",\"description\":\"Basic banking account for corporate customers\"},{\"code\":\"FX\",\"name\":\"Foreign Exchange\",\"description\":\"Foreign exchange trading and hedging services\"},{\"code\":\"TRADING\",\"name\":\"Securities Trading\",\"description\":\"Securities and derivatives trading account\"}]}")
            ))
    public Map<String, Object> getCorporateProducts() {
        return getCorporateProductsFunc.apply(null);
    }
}
