package com.example.kyc.function;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cloud-agnostic serverless functions for KYC requirements.
 * Works with: AWS Lambda, Azure Functions, Google Cloud Functions, or as REST endpoints.
 */
@Configuration
public class KycFunctions {

    private static final Logger logger = LoggerFactory.getLogger(KycFunctions.class);

    private final KieContainer kieContainer;

    public KycFunctions(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    /**
     * Main function to get KYC requirements based on customer profile.
     * Exposed as: POST /getKycRequirements (Spring Cloud Function)
     * Or directly invoked as serverless function.
     */
    @Bean
    public Function<Map<String, Object>, Map<String, Object>> getKycRequirements() {
        return request -> {
            logger.info("Processing KYC requirements for: customerType={}, accountType={}",
                    request.get("customerType"), request.get("accountType"));

            // Validate required fields
            if (request.get("customerType") == null || request.get("accountType") == null) {
                return createErrorResponse("customerType and accountType are required");
            }

            // Initialize collections for rule outputs
            List<Map<String, Object>> fieldsList = new ArrayList<>();
            List<String> rulesList = new ArrayList<>();
            List<String> documentsList = new ArrayList<>();
            List<String> instructionsList = new ArrayList<>();
            Map<String, Object> responseData = new HashMap<>();

            // Set defaults
            responseData.put("riskLevel", "LOW");
            responseData.put("enhancedDueDiligenceRequired", false);
            responseData.put("estimatedProcessingDays", 3);

            // Execute rules
            KieSession kieSession = kieContainer.newKieSession();
            try {
                kieSession.setGlobal("fieldsList", fieldsList);
                kieSession.setGlobal("rulesList", rulesList);
                kieSession.setGlobal("documentsList", documentsList);
                kieSession.setGlobal("instructionsList", instructionsList);
                kieSession.setGlobal("responseData", responseData);

                kieSession.insert(request);
                int rulesFired = kieSession.fireAllRules();
                logger.info("Fired {} rules", rulesFired);

            } finally {
                kieSession.dispose();
            }

            // Build response
            return buildResponse(request, fieldsList, rulesList, documentsList, instructionsList, responseData);
        };
    }

    /**
     * Simple health check function.
     */
    @Bean
    public Function<Map<String, Object>, Map<String, Object>> health() {
        return request -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "UP");
            response.put("service", "kyc-rules-service");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return response;
        };
    }

    /**
     * Function to get available customer types.
     */
    @Bean
    public Function<Map<String, Object>, Map<String, Object>> getCustomerTypes() {
        return request -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("customerTypes", Arrays.asList(
                "INDIVIDUAL", "CORPORATE", "SOLE_PROPRIETOR", 
                "PARTNERSHIP", "TRUST", "FOREIGNER"
            ));
            return response;
        };
    }

    /**
     * Function to get available account types.
     */
    @Bean
    public Function<Map<String, Object>, Map<String, Object>> getAccountTypes() {
        return request -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("accountTypes", Arrays.asList(
                "SAVINGS", "CURRENT", "FIXED_DEPOSIT",
                "INVESTMENT", "LOAN", "CREDIT_CARD"
            ));
            return response;
        };
    }

    private Map<String, Object> buildResponse(Map<String, Object> request, 
                                               List<Map<String, Object>> fieldsList,
                                               List<String> rulesList,
                                               List<String> documentsList,
                                               List<String> instructionsList,
                                               Map<String, Object> responseData) {
        
        Map<String, Object> response = new LinkedHashMap<>();
        
        // Reference ID
        response.put("referenceId", UUID.randomUUID().toString());
        
        // Request info
        response.put("customerType", request.get("customerType"));
        response.put("accountType", request.get("accountType"));
        
        // Risk assessment
        response.put("riskLevel", responseData.get("riskLevel"));
        response.put("enhancedDueDiligenceRequired", responseData.get("enhancedDueDiligenceRequired"));
        
        // Group fields by category
        Map<String, List<Map<String, Object>>> fieldsByCategory = fieldsList.stream()
                .collect(Collectors.groupingBy(
                        f -> (String) f.get("category"),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        
        // Sort fields within each category
        fieldsByCategory.values().forEach(fields ->
                fields.sort(Comparator.comparingInt(f -> (Integer) f.get("displayOrder"))));
        
        response.put("fieldsByCategory", fieldsByCategory);
        
        // Count fields
        long requiredCount = fieldsList.stream().filter(f -> (Boolean) f.get("mandatory")).count();
        long optionalCount = fieldsList.size() - requiredCount;
        response.put("totalRequiredFields", requiredCount);
        response.put("totalOptionalFields", optionalCount);
        
        // Remove duplicates from documents
        response.put("requiredDocuments", documentsList.stream().distinct().collect(Collectors.toList()));
        
        // Instructions
        response.put("specialInstructions", instructionsList.stream().distinct().collect(Collectors.toList()));
        
        // Processing time
        response.put("estimatedProcessingDays", responseData.get("estimatedProcessingDays"));
        
        // Metadata
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("appliedRules", rulesList);
        
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return error;
    }
}
