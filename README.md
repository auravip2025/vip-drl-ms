# Singapore KYC Rules Serverless Microservice

A **cloud-agnostic serverless** microservice using Spring Cloud Function and Drools rules engine for Singapore bank KYC requirements.

## Features

- **Cloud Agnostic Serverless**: Runs on AWS Lambda, Azure Functions, Google Cloud Functions, or as a traditional web app
- **DRL-Based Rules**: All KYC rules defined in `.drl` files for easy modification without code changes
- **Nested JSON Schema Output**: Organized by category for easy form generation
- **Singapore MAS Compliant**: Rules based on MAS (Monetary Authority of Singapore) guidelines
- **Product-Specific Rules**: Corporate KYC varies by product (CASA, FX, Trading)
- **Spring Cloud Function**: Single codebase for all cloud platforms

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Spring Cloud Function                 │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   │
│  │    AWS      │   │   Azure     │   │    GCP      │   │
│  │   Lambda    │   │  Functions  │   │  Functions  │   │
│  └─────────────┘   └─────────────┘   └─────────────┘   │
├─────────────────────────────────────────────────────────┤
│                  KYC Functions                           │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Individual KYC | Corporate KYC (CASA/FX/Trade) │    │
│  └─────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────┤
│                  Drools Rules Engine                     │
│  ┌─────────────────────────────────────────────────┐    │
│  │  SingaporeKycRules.drl | CorporateKycRules.drl  │    │
│  │   (20+ rules per file, easily modifiable)       │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## API Endpoints

### Individual KYC
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/kyc/requirements` | Get individual KYC requirements (detailed) |
| POST | `/api/v1/kyc/product/requirements` | Get individual KYC requirements by product |
| GET | `/api/v1/kyc/health` | Health check |
| GET | `/api/v1/kyc/customer-types` | List customer types |
| GET | `/api/v1/kyc/account-types` | List account types (simple list) |
| GET | `/api/v1/kyc/products` | List individual products with descriptions |
| GET | `/api/v1/kyc/supported-countries` | List supported countries from rules |

### Corporate KYC
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/kyc/corporate/requirements` | Get corporate KYC requirements |
| GET | `/api/v1/kyc/corporate/products` | List available products |

### Serverless Function Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/getKycRequirements` | Individual KYC (detailed) |
| POST | `/getIndividualProductKycRequirements` | Individual KYC by product |
| POST | `/health` | Health check |
| POST | `/getCustomerTypes` | Customer types |
| POST | `/getAccountTypes` | Account types |
| POST | `/getIndividualProducts` | Individual products |
| POST | `/getCorporateProducts` | Corporate products |
| POST | `/getSupportedCountries` | Supported countries |

## API Documentation

### Swagger UI (Interactive Documentation)
Once the service is running, access the interactive API documentation at:

**Swagger UI**: http://localhost:8080/swagger-ui.html

This provides:
- Interactive API testing
- Request/response examples
- Schema definitions
- Try-it-out functionality for all endpoints

### OpenAPI Specification
**OpenAPI JSON**: http://localhost:8080/v3/api-docs  
**OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

You can import these into tools like Postman, Insomnia, or use them for code generation.

## Deployment Options

### 1. Local/Container (Docker)

```bash
# Build
docker run --rm -v "$(pwd):/app" -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests

# Run
docker run -p 8080:8080 -v "$(pwd):/app" -w /app eclipse-temurin:21-jre java -jar target/kyc-rules-service-1.0.0.jar
```

### 2. AWS Lambda

```bash
# Build with AWS profile
mvn clean package -Paws -DskipTests

# Deploy the shaded JAR: target/kyc-rules-service-1.0.0-aws.jar
# Handler: org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest
```

### 3. Azure Functions

```bash
# Build with Azure profile
mvn clean package -Pazure -DskipTests
```

### 4. Google Cloud Functions

```bash
# Build with GCP profile
mvn clean package -Pgcp -DskipTests
```

```
# Step 1: Build the image
docker build -t kyc-service .
# Step 2: Run the container
docker run -d --name kyc-service -p 8080:8080 kyc-service
```

## Usage Examples

### Individual KYC (PowerShell)

```powershell
# Get KYC requirements for individual opening savings account
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/kyc/requirements" `
  -Method POST -ContentType "application/json" `
  -Body '{"customerType": "INDIVIDUAL", "accountType": "SAVINGS", "nationality": "SINGAPORE", "pep": false}' `
  | ConvertTo-Json -Depth 10 | Out-File -FilePath individual-kyc.json
```

### Corporate KYC (PowerShell)

#### 1. Get Available Products
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/kyc/corporate/products" `
  -Method GET | ConvertTo-Json -Depth 3
```

**Response:**
```json
{
  "products": [
    {
      "code": "CASA",
      "name": "Current Account Savings Account",
      "description": "Basic banking account for corporate customers"
    },
    {
      "code": "FX",
      "name": "Foreign Exchange",
      "description": "Foreign exchange trading and hedging services"
    },
    {
      "code": "TRADING",
      "name": "Securities Trading",
      "description": "Securities and derivatives trading account"
    }
  ]
}
```

#### 2. Get Corporate KYC for CASA
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/kyc/corporate/requirements" `
  -Method POST -ContentType "application/json" `
  -Body '{"product": "CASA"}' `
  | ConvertTo-Json -Depth 10 | Out-File -FilePath corporate-casa.json
```

#### 3. Get Corporate KYC for FX (Foreign Exchange)
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/kyc/corporate/requirements" `
  -Method POST -ContentType "application/json" `
  -Body '{"product": "FX"}' `
  | ConvertTo-Json -Depth 10 | Out-File -FilePath corporate-fx.json
```

#### 4. Get Corporate KYC for TRADING
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/kyc/corporate/requirements" `
  -Method POST -ContentType "application/json" `
  -Body '{"product": "TRADING"}' `
  | ConvertTo-Json -Depth 10 | Out-File -FilePath corporate-trading.json
```

#### 5. View Summary Only
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/kyc/corporate/requirements" `
  -Method POST -ContentType "application/json" `
  -Body '{"product": "FX"}'

# Show summary
$response.'x-metadata' | Select-Object product, riskLevel, enhancedDueDiligenceRequired, `
  totalRequiredFields, estimatedProcessingDays, categories | ConvertTo-Json
```

#### 6. View Specific Category
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/kyc/corporate/requirements" `
  -Method POST -ContentType "application/json" `
  -Body '{"product": "FX"}'

# View FX-specific fields
$response.properties.product_fx | ConvertTo-Json -Depth 5
```

#### 7. Quick Test All Products
```powershell
@("CASA", "FX", "TRADING") | ForEach-Object {
    $product = $_
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/kyc/corporate/requirements" `
      -Method POST -ContentType "application/json" `
      -Body "{`"product`": `"$product`"}"
    
    Write-Host "`n=== $product ===" -ForegroundColor Cyan
    Write-Host "Risk Level: $($result.'x-metadata'.riskLevel)" -ForegroundColor Yellow
    Write-Host "Required Fields: $($result.'x-metadata'.totalRequiredFields)"
    Write-Host "Processing Days: $($result.'x-metadata'.estimatedProcessingDays)"
    Write-Host "Categories: $($result.properties.Keys -join ', ')"
}
```

## JSON Schema Structure

The service returns nested JSON Schema organized by category:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Singapore KYC Form",
  "type": "object",
  "properties": {
    "personal_details": {
      "type": "object",
      "title": "Personal Details",
      "properties": {
        "full_name": {
          "type": "string",
          "title": "Full Name (as per NRIC/Passport)",
          "description": "Enter your full legal name...",
          "pattern": "^[a-zA-Z\\s]{2,100}$"
        }
      },
      "required": ["full_name", "date_of_birth", "gender", "nationality"]
    },
    "identification": {...},
    "contact_details": {...}
  },
  "required": ["personal_details", "identification", "contact_details"],
  "x-metadata": {
    "referenceId": "uuid",
    "riskLevel": "LOW",
    "totalRequiredFields": 17,
    "estimatedProcessingDays": 3,
    "categories": ["Personal Details", "Identification", "Contact Details", ...]
  }
}
```

## Corporate Products Comparison

| Product | Risk Level | Required Fields | Processing Days | Key Requirements |
|---------|-----------|-----------------|-----------------|------------------|
| **CASA** | LOW | ~30 | 7 | Basic company info, directors, account details |
| **FX** | MEDIUM | ~35 | 10 | + FX experience, risk policy, authorized dealers |
| **TRADING** | HIGH | ~40 | 14 | + Trading strategy, compliance officer, authorization matrix |

## Modifying KYC Rules

Edit the DRL files to add/modify fields:

### Individual KYC
`src/main/resources/rules/SingaporeKycRules.drl`

### Corporate KYC
`src/main/resources/rules/CorporateKycRules.drl`

**No Java code changes required!** Just edit the DRL and redeploy.

## Project Structure

```
src/main/
├── java/com/example/kyc/
│   ├── KycRulesApplication.java           # Spring Boot app
│   ├── config/DroolsConfig.java           # Drools configuration
│   ├── controller/KycController.java      # REST endpoints
│   └── function/KycFunctions.java         # Serverless functions
└── resources/
    ├── application.yml                    # Spring config
    └── rules/
        ├── SingaporeKycRules.drl          # Individual KYC rules
        └── CorporateKycRules.drl          # Corporate KYC rules
```

## Customer Types
`INDIVIDUAL`, `CORPORATE`, `SOLE_PROPRIETOR`, `PARTNERSHIP`, `TRUST`, `FOREIGNER`

## Account Types (Individual)
`SAVINGS`, `CURRENT`, `FIXED_DEPOSIT`, `INVESTMENT`, `LOAN`, `CREDIT_CARD`

## Products (Corporate)
`CASA`, `FX`, `TRADING`
