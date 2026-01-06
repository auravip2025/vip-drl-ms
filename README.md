# Singapore KYC Rules Serverless Microservice

A **cloud-agnostic serverless** microservice using Spring Cloud Function and Drools rules engine for Singapore bank KYC requirements.

## Features

- **Cloud Agnostic Serverless**: Runs on AWS Lambda, Azure Functions, Google Cloud Functions, or as a traditional web app
- **DRL-Based Rules**: All KYC rules defined in `.drl` files for easy modification without code changes
- **Dynamic Models**: Uses Map-based facts for flexible data structures defined in rules
- **Singapore MAS Compliant**: Rules based on MAS (Monetary Authority of Singapore) guidelines
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
│  │  getKycRequirements | health | getCustomerTypes │    │
│  └─────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────┤
│                  Drools Rules Engine                     │
│  ┌─────────────────────────────────────────────────┐    │
│  │            SingaporeKycRules.drl                 │    │
│  │   (20+ rules: Personal, ID, Tax, PEP, etc.)     │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## API Endpoints

### REST Endpoints (Web App Mode)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/kyc/requirements` | Get KYC requirements |
| GET | `/api/v1/kyc/health` | Health check |
| GET | `/api/v1/kyc/customer-types` | List customer types |
| GET | `/api/v1/kyc/account-types` | List account types |

### Function Endpoints (Serverless Mode)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/getKycRequirements` | Get KYC requirements |
| POST | `/health` | Health check |
| POST | `/getCustomerTypes` | List customer types |
| POST | `/getAccountTypes` | List account types |

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

# Follow Azure Functions deployment documentation
```

### 4. Google Cloud Functions

```bash
# Build with GCP profile
mvn clean package -Pgcp -DskipTests

# Deploy using gcloud functions deploy
```

## Sample Request

```bash
curl -X POST http://localhost:8080/api/v1/kyc/requirements \
  -H "Content-Type: application/json" \
  -d '{
    "customerType": "INDIVIDUAL",
    "accountType": "SAVINGS",
    "nationality": "SINGAPORE",
    "pep": false
  }'
```

## Sample Response

```json
{
  "referenceId": "uuid",
  "customerType": "INDIVIDUAL",
  "accountType": "SAVINGS",
  "riskLevel": "LOW",
  "enhancedDueDiligenceRequired": false,
  "fieldsByCategory": {
    "PERSONAL_DETAILS": [...],
    "IDENTIFICATION": [...],
    "CONTACT_DETAILS": [...],
    "EMPLOYMENT": [...],
    "TAX_INFORMATION": [...],
    "DECLARATIONS": [...]
  },
  "totalRequiredFields": 17,
  "requiredDocuments": ["NRIC Front", "NRIC Back"],
  "estimatedProcessingDays": 3,
  "appliedRules": [...]
}
```

## Modifying KYC Rules

Edit `src/main/resources/rules/SingaporeKycRules.drl` to:
- Add new fields
- Modify validation patterns
- Add new customer/account types
- Change risk assessment logic

No Java code changes required!

## Project Structure

```
src/main/
├── java/com/example/kyc/
│   ├── KycRulesApplication.java      # Spring Boot app
│   ├── config/DroolsConfig.java      # Drools configuration
│   ├── controller/KycController.java # REST endpoints
│   └── function/KycFunctions.java    # Serverless functions
└── resources/
    ├── application.yml               # Spring config
    └── rules/
        └── SingaporeKycRules.drl     # KYC rules (easy to modify!)
```

## Customer Types
`INDIVIDUAL`, `CORPORATE`, `SOLE_PROPRIETOR`, `PARTNERSHIP`, `TRUST`, `FOREIGNER`

## Account Types
`SAVINGS`, `CURRENT`, `FIXED_DEPOSIT`, `INVESTMENT`, `LOAN`, `CREDIT_CARD`
