package com.example.kyc.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.function.Function;

/**
 * REST Controller that exposes the serverless functions as REST endpoints.
 * This provides a consistent API whether running as a web app or serverless function.
 */
@RestController
@RequestMapping("/api/v1/kyc")
public class KycController {

    private final Function<Map<String, Object>, Map<String, Object>> getKycRequirements;
    private final Function<Map<String, Object>, Map<String, Object>> health;
    private final Function<Map<String, Object>, Map<String, Object>> getCustomerTypes;
    private final Function<Map<String, Object>, Map<String, Object>> getAccountTypes;

    public KycController(
            Function<Map<String, Object>, Map<String, Object>> getKycRequirements,
            Function<Map<String, Object>, Map<String, Object>> health,
            Function<Map<String, Object>, Map<String, Object>> getCustomerTypes,
            Function<Map<String, Object>, Map<String, Object>> getAccountTypes) {
        this.getKycRequirements = getKycRequirements;
        this.health = health;
        this.getCustomerTypes = getCustomerTypes;
        this.getAccountTypes = getAccountTypes;
    }

    @PostMapping("/requirements")
    public Map<String, Object> getRequirements(@RequestBody Map<String, Object> request) {
        return getKycRequirements.apply(request);
    }

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return health.apply(null);
    }

    @GetMapping("/customer-types")
    public Map<String, Object> customerTypes() {
        return getCustomerTypes.apply(null);
    }

    @GetMapping("/account-types")
    public Map<String, Object> accountTypes() {
        return getAccountTypes.apply(null);
    }
}
