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

    /**
     * Function to get corporate KYC requirements based on product.
     * Products: CASA, FX, TRADING
     */
    @Bean
    public Function<Map<String, Object>, Map<String, Object>> getCorporateKycRequirements() {
        return request -> {
            logger.info("Processing Corporate KYC requirements for product: {}", request.get("product"));

            // Validate required fields
            if (request.get("product") == null) {
                return createErrorResponse("product is required (CASA, FX, or TRADING)");
            }

            // Set customer type to CORPORATE
            request.put("customerType", "CORPORATE");

            // Initialize collections for rule outputs
            List<Map<String, Object>> fieldsList = new ArrayList<>();
            List<String> rulesList = new ArrayList<>();
            List<String> documentsList = new ArrayList<>();
            List<String> instructionsList = new ArrayList<>();
            Map<String, Object> responseData = new HashMap<>();

            // Set defaults
            responseData.put("riskLevel", "LOW");
            responseData.put("enhancedDueDiligenceRequired", false);
            responseData.put("estimatedProcessingDays", 7);

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
                logger.info("Fired {} rules for corporate KYC", rulesFired);

            } finally {
                kieSession.dispose();
            }

            // Build response
            return buildCorporateResponse(request, fieldsList, rulesList, documentsList, instructionsList, responseData);
        };
    }

    /**
     * Function to get available corporate products.
     */
    @Bean
    public Function<Map<String, Object>, Map<String, Object>> getCorporateProducts() {
        return request -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("products", Arrays.asList(
                Map.of("code", "CASA", "name", "Current Account Savings Account", "description", "Basic banking account for corporate customers"),
                Map.of("code", "FX", "name", "Foreign Exchange", "description", "Foreign exchange trading and hedging services"),
                Map.of("code", "TRADING", "name", "Securities Trading", "description", "Securities and derivatives trading account")
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
        
        // Metadata
        response.put("$schema", "http://json-schema.org/draft-07/schema#");
        response.put("title", "Singapore KYC Form");
        response.put("description", "KYC requirements for " + request.get("customerType") + " opening " + request.get("accountType") + " account");
        response.put("type", "object");
        
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
        
        // Build nested properties by category
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> requiredCategories = new ArrayList<>();
        
        for (Map.Entry<String, List<Map<String, Object>>> categoryEntry : fieldsByCategory.entrySet()) {
            String category = categoryEntry.getKey();
            List<Map<String, Object>> categoryFields = categoryEntry.getValue();
            
            // Create category object
            Map<String, Object> categorySchema = new LinkedHashMap<>();
            categorySchema.put("type", "object");
            categorySchema.put("title", formatCategoryName(category));
            categorySchema.put("description", "Fields related to " + formatCategoryName(category).toLowerCase());
            
            // Build properties for this category
            Map<String, Object> categoryProperties = new LinkedHashMap<>();
            List<String> categoryRequired = new ArrayList<>();
            
            for (Map<String, Object> field : categoryFields) {
                String fieldId = (String) field.get("fieldId");
                Map<String, Object> property = new LinkedHashMap<>();
                
                // Set type based on fieldType
                String fieldType = (String) field.get("fieldType");
                switch (fieldType) {
                    case "NUMBER":
                        property.put("type", "number");
                        break;
                    case "CHECKBOX":
                        property.put("type", "boolean");
                        break;
                    case "DATE":
                        property.put("type", "string");
                        property.put("format", "date");
                        break;
                    case "EMAIL":
                        property.put("type", "string");
                        property.put("format", "email");
                        break;
                    case "PHONE":
                        property.put("type", "string");
                        property.put("pattern", "^[0-9]{8,15}$");
                        break;
                    case "DOCUMENT":
                        property.put("type", "string");
                        property.put("format", "uri");
                        property.put("contentMediaType", "application/pdf");
                        break;
                    case "ADDRESS":
                        property.put("type", "object");
                        property.put("properties", Map.of(
                            "street", Map.of("type", "string"),
                            "city", Map.of("type", "string"),
                            "postalCode", Map.of("type", "string"),
                            "country", Map.of("type", "string")
                        ));
                        break;
                    default:
                        property.put("type", "string");
                }
                
                // Add title and description
                property.put("title", field.get("fieldName"));
                property.put("description", field.get("description"));
                
                // Add validation pattern if present
                if (field.get("validationPattern") != null) {
                    property.put("pattern", field.get("validationPattern"));
                }
                
                // Add custom properties for UI hints
                Map<String, Object> uiHints = new LinkedHashMap<>();
                uiHints.put("displayOrder", field.get("displayOrder"));
                uiHints.put("fieldType", fieldType);
                if (field.get("documentRequired") != null && (Boolean) field.get("documentRequired")) {
                    uiHints.put("documentRequired", true);
                    uiHints.put("acceptedDocuments", field.get("acceptedDocuments"));
                }
                if (field.get("additionalNotes") != null) {
                    uiHints.put("additionalNotes", field.get("additionalNotes"));
                }
                property.put("x-ui-hints", uiHints);
                
                categoryProperties.put(fieldId, property);
                
                // Add to required array if mandatory
                if ((Boolean) field.get("mandatory")) {
                    categoryRequired.add(fieldId);
                }
            }
            
            categorySchema.put("properties", categoryProperties);
            if (!categoryRequired.isEmpty()) {
                categorySchema.put("required", categoryRequired);
            }
            
            // Add category order hint
            categorySchema.put("x-category-order", fieldsByCategory.keySet().stream()
                    .collect(Collectors.toList()).indexOf(category));
            
            properties.put(category.toLowerCase(), categorySchema);
            
            // If category has required fields, mark category as required
            if (!categoryRequired.isEmpty()) {
                requiredCategories.add(category.toLowerCase());
            }
        }
        
        response.put("properties", properties);
        response.put("required", requiredCategories);
        
        // Count total fields
        long totalRequiredFields = fieldsList.stream().filter(f -> (Boolean) f.get("mandatory")).count();
        long totalOptionalFields = fieldsList.size() - totalRequiredFields;
        
        // Additional metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("referenceId", UUID.randomUUID().toString());
        metadata.put("customerType", request.get("customerType"));
        metadata.put("accountType", request.get("accountType"));
        metadata.put("riskLevel", responseData.get("riskLevel"));
        metadata.put("enhancedDueDiligenceRequired", responseData.get("enhancedDueDiligenceRequired"));
        metadata.put("totalRequiredFields", totalRequiredFields);
        metadata.put("totalOptionalFields", totalOptionalFields);
        metadata.put("requiredDocuments", documentsList.stream().distinct().collect(Collectors.toList()));
        metadata.put("specialInstructions", instructionsList.stream().distinct().collect(Collectors.toList()));
        metadata.put("estimatedProcessingDays", responseData.get("estimatedProcessingDays"));
        metadata.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metadata.put("appliedRules", rulesList);
        metadata.put("categories", fieldsByCategory.keySet().stream()
                .map(this::formatCategoryName)
                .collect(Collectors.toList()));
        
        response.put("x-metadata", metadata);
        
        return response;
    }
    
    private String formatCategoryName(String category) {
        return Arrays.stream(category.split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private Map<String, Object> buildCorporateResponse(Map<String, Object> request, 
                                                        List<Map<String, Object>> fieldsList,
                                                        List<String> rulesList,
                                                        List<String> documentsList,
                                                        List<String> instructionsList,
                                                        Map<String, Object> responseData) {
        
        Map<String, Object> response = new LinkedHashMap<>();
        
        // Metadata
        String product = (String) request.get("product");
        response.put("$schema", "http://json-schema.org/draft-07/schema#");
        response.put("title", "Singapore Corporate KYC Form - " + product);
        response.put("description", "Corporate KYC requirements for " + product + " product");
        response.put("type", "object");
        
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
        
        // Build nested properties by category
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> requiredCategories = new ArrayList<>();
        
        for (Map.Entry<String, List<Map<String, Object>>> categoryEntry : fieldsByCategory.entrySet()) {
            String category = categoryEntry.getKey();
            List<Map<String, Object>> categoryFields = categoryEntry.getValue();
            
            // Create category object
            Map<String, Object> categorySchema = new LinkedHashMap<>();
            categorySchema.put("type", "object");
            categorySchema.put("title", formatCategoryName(category));
            categorySchema.put("description", "Fields related to " + formatCategoryName(category).toLowerCase());
            
            // Build properties for this category
            Map<String, Object> categoryProperties = new LinkedHashMap<>();
            List<String> categoryRequired = new ArrayList<>();
            
            for (Map<String, Object> field : categoryFields) {
                String fieldId = (String) field.get("fieldId");
                Map<String, Object> property = new LinkedHashMap<>();
                
                // Set type based on fieldType
                String fieldType = (String) field.get("fieldType");
                switch (fieldType) {
                    case "NUMBER":
                        property.put("type", "number");
                        break;
                    case "CHECKBOX":
                        property.put("type", "boolean");
                        break;
                    case "DATE":
                        property.put("type", "string");
                        property.put("format", "date");
                        break;
                    case "EMAIL":
                        property.put("type", "string");
                        property.put("format", "email");
                        break;
                    case "PHONE":
                        property.put("type", "string");
                        property.put("pattern", "^[0-9]{8,15}$");
                        break;
                    case "DOCUMENT":
                        property.put("type", "string");
                        property.put("format", "uri");
                        property.put("contentMediaType", "application/pdf");
                        break;
                    case "ADDRESS":
                        property.put("type", "object");
                        property.put("properties", Map.of(
                            "street", Map.of("type", "string"),
                            "city", Map.of("type", "string"),
                            "postalCode", Map.of("type", "string"),
                            "country", Map.of("type", "string")
                        ));
                        break;
                    default:
                        property.put("type", "string");
                }
                
                // Add title and description
                property.put("title", field.get("fieldName"));
                property.put("description", field.get("description"));
                
                // Add validation pattern if present
                if (field.get("validationPattern") != null) {
                    property.put("pattern", field.get("validationPattern"));
                }
                
                // Add custom properties for UI hints
                Map<String, Object> uiHints = new LinkedHashMap<>();
                uiHints.put("displayOrder", field.get("displayOrder"));
                uiHints.put("fieldType", fieldType);
                if (field.get("documentRequired") != null && (Boolean) field.get("documentRequired")) {
                    uiHints.put("documentRequired", true);
                    uiHints.put("acceptedDocuments", field.get("acceptedDocuments"));
                }
                if (field.get("additionalNotes") != null) {
                    uiHints.put("additionalNotes", field.get("additionalNotes"));
                }
                property.put("x-ui-hints", uiHints);
                
                categoryProperties.put(fieldId, property);
                
                // Add to required array if mandatory
                if ((Boolean) field.get("mandatory")) {
                    categoryRequired.add(fieldId);
                }
            }
            
            categorySchema.put("properties", categoryProperties);
            if (!categoryRequired.isEmpty()) {
                categorySchema.put("required", categoryRequired);
            }
            
            // Add category order hint
            categorySchema.put("x-category-order", fieldsByCategory.keySet().stream()
                    .collect(Collectors.toList()).indexOf(category));
            
            properties.put(category.toLowerCase(), categorySchema);
            
            // If category has required fields, mark category as required
            if (!categoryRequired.isEmpty()) {
                requiredCategories.add(category.toLowerCase());
            }
        }
        
        response.put("properties", properties);
        response.put("required", requiredCategories);
        
        // Count total fields
        long totalRequiredFields = fieldsList.stream().filter(f -> (Boolean) f.get("mandatory")).count();
        long totalOptionalFields = fieldsList.size() - totalRequiredFields;
        
        // Additional metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("referenceId", UUID.randomUUID().toString());
        metadata.put("customerType", "CORPORATE");
        metadata.put("product", product);
        metadata.put("productType", responseData.get("productType"));
        metadata.put("riskLevel", responseData.get("riskLevel"));
        metadata.put("enhancedDueDiligenceRequired", responseData.get("enhancedDueDiligenceRequired"));
        metadata.put("totalRequiredFields", totalRequiredFields);
        metadata.put("totalOptionalFields", totalOptionalFields);
        metadata.put("requiredDocuments", documentsList.stream().distinct().collect(Collectors.toList()));
        metadata.put("specialInstructions", instructionsList.stream().distinct().collect(Collectors.toList()));
        metadata.put("estimatedProcessingDays", responseData.get("estimatedProcessingDays"));
        metadata.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        metadata.put("appliedRules", rulesList);
        metadata.put("categories", fieldsByCategory.keySet().stream()
                .map(this::formatCategoryName)
                .collect(Collectors.toList()));
        
        response.put("x-metadata", metadata);
        
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
